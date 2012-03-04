/*
 * XFinder.cpp
 *
 *  Created on: 2011. 1. 10.
 *      Author: zerom
 */

#include <stdlib.h>

#include "XFinder.h"
#include "ImgProcess.h"
#include <queue>

using namespace Robot;

XFinder::XFinder() :
        m_hue(356),
        m_hue_tolerance(15),
        m_min_saturation(50),
        m_min_value(10),
        m_min_percent(0.07),
        m_max_percent(30.0),
        color_section(""),
        m_result(0),
        m_visited(0)
{ }

XFinder::XFinder(int hue, int hue_tol, int min_sat, int min_val, double min_per, double max_per) :
        m_hue(hue),
        m_hue_tolerance(hue_tol),
        m_min_saturation(min_sat),
        m_min_value(min_val),
        m_min_percent(min_per),
        m_max_percent(max_per),
        color_section(""),
        m_result(0),
        m_visited(0)
{ }

XFinder::~XFinder()
{
    // TODO Auto-generated destructor stub
}

void XFinder::Filtering(Image *img)
{
    unsigned int h, s, v;
    int h_max, h_min;

    if(m_result == NULL)
        m_result = new Image(img->m_Width, img->m_Height, 1);

    h_max = m_hue + m_hue_tolerance;
    h_min = m_hue - m_hue_tolerance;
    if(h_max > 360)
        h_max -= 360;
    if(h_min < 0)
        h_min += 360;

    for(int i = 0; i < img->m_NumberOfPixels; i++)
    {
        h = (img->m_ImageData[i*img->m_PixelSize + 0] << 8) | img->m_ImageData[i*img->m_PixelSize + 1];
        s =  img->m_ImageData[i*img->m_PixelSize + 2];
        v =  img->m_ImageData[i*img->m_PixelSize + 3];

        if( h > 360 )
            h = h % 360;

        if( ((int)s > m_min_saturation) && ((int)v > m_min_value) )
        {
            if(h_min <= h_max)
            {
                if((h_min < (int)h) && ((int)h < h_max))
                    m_result->m_ImageData[i]= 1;
                else
                    m_result->m_ImageData[i]= 0;
            }
            else
            {
                if((h_min < (int)h) || ((int)h < h_max))
                    m_result->m_ImageData[i]= 1;
                else
                    m_result->m_ImageData[i]= 0;
            }
        }
        else
        {
            m_result->m_ImageData[i]= 0;
        }
    }
}


void XFinder::FloodFill(int x, int y, Point2D* x_center)
{
  int sum_x = 0, sum_y = 0, count;

  std::queue <Point2D> unprocessed;
  unprocessed.push(Point2D(x, y));
  
  while (!unprocessed.empty()) {
    Point2D& current = unprocessed.front();
    sum_x += current.X;
    sum_y += current.Y;
    m_visited->m_ImageData[m_result->m_Width * (int) current.Y + (int) current.X] = 1;
    count++;
    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1 ; j++) {
        if (i == 0 && j == 0) continue;
        int nx = current.X + i;
        int ny = current.Y + j;
        if (nx < 0 || nx >= m_result->m_Width ||
            ny < 0 || ny >= m_result->m_Height) continue;
        if(m_result->m_ImageData[m_result->m_Width * ny + nx] > 0 &&
           m_visited->m_ImageData[m_result->m_Width * ny + nx] == 0) {
          unprocessed.push(Point2D(nx, ny));
        }
      }
    }
    unprocessed.pop();
  }

  if(count <= (m_result->m_NumberOfPixels * m_min_percent / 100) ||
     count > (m_result->m_NumberOfPixels * m_max_percent / 100))
  {
      x_center->X = -1.0;
      x_center->Y = -1.0;
  }
  else
  {
      x_center->X = (int)((double)sum_x / (double)count);
      x_center->Y = (int)((double)sum_y / (double)count);
  }
}

void XFinder::LoadINISettings(minIni* ini)
{
    LoadINISettings(ini, COLOR_SECTION);
}

void XFinder::LoadINISettings(minIni* ini, const std::string &section)
{
    int value = -2;
    if((value = ini->geti(section, "hue", INVALID_VALUE)) != INVALID_VALUE)             m_hue = value;
    if((value = ini->geti(section, "hue_tolerance", INVALID_VALUE)) != INVALID_VALUE)   m_hue_tolerance = value;
    if((value = ini->geti(section, "min_saturation", INVALID_VALUE)) != INVALID_VALUE)  m_min_saturation = value;
    if((value = ini->geti(section, "min_value", INVALID_VALUE)) != INVALID_VALUE)       m_min_value = value;

    double dvalue = -2.0;
    if((dvalue = ini->getd(section, "min_percent", INVALID_VALUE)) != INVALID_VALUE)    m_min_percent = dvalue;
    if((dvalue = ini->getd(section, "max_percent", INVALID_VALUE)) != INVALID_VALUE)    m_max_percent = dvalue;

    color_section = section;
}

void XFinder::SaveINISettings(minIni* ini)
{
    SaveINISettings(ini, COLOR_SECTION);
}

void XFinder::SaveINISettings(minIni* ini, const std::string &section)
{
    ini->put(section,   "hue",              m_hue);
    ini->put(section,   "hue_tolerance",    m_hue_tolerance);
    ini->put(section,   "min_saturation",   m_min_saturation);
    ini->put(section,   "min_value",        m_min_value);
    ini->put(section,   "min_percent",      m_min_percent);
    ini->put(section,   "max_percent",      m_max_percent);

    color_section = section;
}

int XFinder::GetPositions(Image* hsv_img, Point2D* results)
{
  int nbXFound = 0;

  Filtering(hsv_img);

  ImgProcess::Erosion(m_result);
  ImgProcess::Dilation(m_result);

  if(m_visited == NULL)
    m_visited = new Image(m_result->m_Width, m_result->m_Height, 1);

  for(int y = 0; y < m_result->m_Height; y++)
  {
    for(int x = 0; x < m_result->m_Width; x++)
    {
      if(m_result->m_ImageData[m_result->m_Width * y + x] > 0 &&
         m_visited->m_ImageData[m_result->m_Width * y + x] == 0)
      {
        FloodFill(x, y, &results[nbXFound]);
        if (results[nbXFound].X >= 0) {
          nbXFound++;
        }
      }
    }
  }

  return nbXFound;
}
