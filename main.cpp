/*
 * main.cpp
 *
 *  Created on: 2011. 1. 4.
 *      Author: robotis
 */

#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <libgen.h>
#include "Camera.h"
#include "mjpg_streamer.h"
#include "LinuxDARwIn.h"
#include "XFinder.h"

#define INI_FILE_PATH       "config.ini"
#define U2D_DEV_NAME        "/dev/ttyUSB0"

using namespace Robot;


void change_current_dir()
{
  char exepath[1024] = {0};
  if(readlink("/proc/self/exe", exepath, sizeof(exepath)) != -1)
    chdir(dirname(exepath));
}

int main(void)
{
  printf( "\n===== Roogle's Dodo =====\n\n");

  change_current_dir();

  Image* rgb_ball = new Image(Camera::WIDTH, Camera::HEIGHT, Image::RGB_PIXEL_SIZE);

  minIni* ini = new minIni(INI_FILE_PATH);

  LinuxCamera::GetInstance()->Initialize(0);
  LinuxCamera::GetInstance()->LoadINISettings(ini);

  mjpg_streamer* streamer = new mjpg_streamer(Camera::WIDTH, Camera::HEIGHT);

  XFinder* ball_finder = new XFinder();
  ball_finder->LoadINISettings(ini);
//  httpd::ball_finder = ball_finder;

  BallTracker tracker = BallTracker();
  BallFollower follower = BallFollower();
  follower.DEBUG_PRINT = true;

  //////////////////// Framework Initialize ////////////////////////////
  LinuxCM730 linux_cm730(U2D_DEV_NAME);
  CM730 cm730(&linux_cm730);
  if(MotionManager::GetInstance()->Initialize(&cm730) == false)
  {
    printf("Fail to initialize Motion Manager!\n");
      return 0;
  }
  MotionManager::GetInstance()->AddModule((MotionModule*)Head::GetInstance());
  MotionManager::GetInstance()->AddModule((MotionModule*)Walking::GetInstance());
  LinuxMotionTimer::Initialize(MotionManager::GetInstance()); 
  /////////////////////////////////////////////////////////////////////

  int n = 0;
  int param[JointData::NUMBER_OF_JOINTS * 5];
  int wGoalPosition, wStartPosition, wDistance;

  for(int id=JointData::ID_R_SHOULDER_PITCH; id<JointData::NUMBER_OF_JOINTS; id++)
  {
    wStartPosition = MotionStatus::m_CurrentJoints.GetValue(id);
    wGoalPosition = Walking::GetInstance()->m_Joint.GetValue(id);
    if( wStartPosition > wGoalPosition )
      wDistance = wStartPosition - wGoalPosition;
    else
      wDistance = wGoalPosition - wStartPosition;

    wDistance >>= 2;
    if( wDistance < 8 )
      wDistance = 8;

    param[n++] = id;
    param[n++] = CM730::GetLowByte(wGoalPosition);
    param[n++] = CM730::GetHighByte(wGoalPosition);
    param[n++] = CM730::GetLowByte(wDistance);
    param[n++] = CM730::GetHighByte(wDistance);
  }
  cm730.SyncWrite(MX28::P_GOAL_POSITION_L, 5, JointData::NUMBER_OF_JOINTS - 1, param);  

  printf("Press the ENTER key to begin!\n");
  getchar();
  
  Head::GetInstance()->m_Joint.SetEnableHeadOnly(true, true);
  Walking::GetInstance()->m_Joint.SetEnableBodyWithoutHead(true, true);
  MotionManager::GetInstance()->SetEnable(true);

  Point2D positions[100];
  int nbFailed = 0;
  while(1)
  {
    LinuxCamera::GetInstance()->CaptureFrame();

    memcpy(rgb_ball->m_ImageData, LinuxCamera::GetInstance()->fbuffer->m_RGBFrame->m_ImageData, LinuxCamera::GetInstance()->fbuffer->m_RGBFrame->m_ImageSize);

    int nbXFound = ball_finder->GetPositions(LinuxCamera::GetInstance()->fbuffer->m_HSVFrame, positions);

    Point2D lookAt(-1, -1);
    Point2D walkTo(-1, -1);
    int dir = 0;
    if (nbXFound > 0) {
      if (nbXFound >= 4) {
        printf("Found %d, average and go!\n", nbXFound);
        double sumX = 0, sumY = 0;
        for (int i = 0; i < nbXFound; ++i) {
          sumX += positions[i].X;
          sumY += positions[i].Y;
        }
        lookAt.X = sumX / nbXFound;
        lookAt.Y = sumY / nbXFound;
        walkTo = lookAt;
      } else if (nbXFound == 3) {
        printf("Found 3, doing some vector arithmetic!\n");
        double l01 = Point2D::Distance(positions[0], positions[1]);
        double l12 = Point2D::Distance(positions[1], positions[2]);
        double l02 = Point2D::Distance(positions[0], positions[2]);
        // Find the longest one
        int corner = 0;
        if (l01 >= l12 && l01 >= l02)
          corner = 2;
        else if (l02 >= l01 && l02 >= l12)
          corner = 1;
        int p0 = (corner + 1) % 3;
        int p1 = (corner + 2) % 3;
        lookAt.X = (positions[p0].X + positions[p1].X) / 2;
        lookAt.Y = (positions[p0].Y + positions[p1].Y) / 2;
        walkTo = lookAt;
      } else if (nbXFound == 2) {
        printf("Found 2!\n");
        lookAt.X = (positions[0].X + positions[1].X) / 2;
        lookAt.Y = (positions[0].Y + positions[1].Y) / 2;
        if (positions[0].X < positions[1].X) {
          if (positions[0].Y < positions[1].Y - 50) {
            dir = 1;
          } else if (positions[1].Y < positions[0].Y - 50) {
            dir = -1;
          }
        } else {
          if (positions[0].Y < positions[1].Y - 50) {
            dir = -1;
          } else if (positions[1].Y < positions[0].Y - 50) {
            dir = 1;
          }
        }
      }
    }

    // if (nbXFound > 0) {
    //   printf("nbXFound: %d\n", nbXFound);
    //   int maxY = ball_finder->m_result->m_Width * 75 / 100;
    //   for (int i = 0; i < nbXFound; ++i) {
    //     if (positions[i].Y < maxY && positions[i].Y > pointToTrack.Y) {
    //       pointToTrack = positions[i];
    //     }
    //   }
    // }

    tracker.Process(lookAt);
    if (walkTo.X < 0) {
      nbFailed++;
    } else {
      nbFailed = 0;
    }
    if (nbFailed >= 8) {
      printf("Failure!");
      nbFailed = 10;
      Walking::GetInstance()->X_MOVE_AMPLITUDE = 0.0;
      Walking::GetInstance()->A_MOVE_AMPLITUDE = 0.0;
      Walking::GetInstance()->Start();
    } else {
      printf("Success!");
      follower.Process(walkTo);
    }
  }

  return 0;
}
