/*
 * ColorFinder.h
 *
 *  Created on: 2011. 1. 10.
 *      Author: zerom
 */

#ifndef XFINDER_H_
#define XFINDER_H_

#include <string>

#include "Point.h"
#include "Image.h"
#include "minIni.h"

#define COLOR_SECTION   "Find Color"
#define INVALID_VALUE   -1024.0

namespace Robot
{
  class XFinder
  {
  private:
    void Filtering(Image* img);

  public:
    int m_hue;             /* 0 ~ 360 */
    int m_hue_tolerance;   /* 0 ~ 180 */
    int m_min_saturation;  /* 0 ~ 100 */
    int m_min_value;       /* 0 ~ 100 */
    double m_min_percent;  /* 0.0 ~ 100.0 */
    double m_max_percent;  /* 0.0 ~ 100.0 */

    std::string color_section;

    Image*  m_result;
    Image*  m_visited;

    XFinder();
    XFinder(int hue, int hue_tol, int min_sat, int min_val, double min_per, double max_per);
    virtual ~XFinder();

    void LoadINISettings(minIni* ini);
    void LoadINISettings(minIni* ini, const std::string &section);
    void SaveINISettings(minIni* ini);
    void SaveINISettings(minIni* ini, const std::string &section);

    void FloodFill(int x, int y, Point2D* x_center);

    int GetPositions(Image* hsv_img, Point2D* results);
  };
}

#endif /* COLORFINDER_H_ */
