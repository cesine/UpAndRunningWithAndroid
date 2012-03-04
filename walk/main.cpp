/*
 * main.cpp
 *
 *  Created on: 2011. 1. 4.
 *      Author: robotis
 */

#include <unistd.h>
#include <string.h>
#include <libgen.h>

#include "Camera.h"
#include "Point.h"
#include "mjpg_streamer.h"
#include "minIni.h"
#include "LinuxCamera.h"
#include "ColorFinder.h"

#include "Action.h"
#include "Head.h"
#include "Walking.h"
#include "MX28.h"
#include "MotionManager.h"
#include "LinuxMotionTimer.h"
#include "LinuxCM730.h"
#include "LinuxActionScript.h"

#ifdef MX28_1024
#define MOTION_FILE_PATH    "../../../../../Data/motion_1024.bin"
#else
#define MOTION_FILE_PATH    "../../../../../Data/motion_4096.bin"
#endif

#define U2D_DEV_NAME        "/dev/ttyUSB0"

void change_current_dir()
{
		char exepath[1024] = {0};
		if(readlink("/proc/self/exe", exepath, sizeof(exepath)) != -1)
				chdir(dirname(exepath));
}

int main(void)
{
		printf( "\n===== Action script Tutorial for DARwIn =====\n\n");

		change_current_dir();

		LinuxCM730 linux_cm730(U2D_DEV_NAME);
		CM730 cm730(&linux_cm730);
		if(MotionManager::GetInstance()->Initialize(&cm730) == false)
		{
				printf("Fail to initialize Motion Manager!\n");
				return 0;
		}


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

}
