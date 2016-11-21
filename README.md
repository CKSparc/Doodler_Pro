# Doodler Pro

##### A simple android application that lets you re-create any image with your fingertips. You can change the brush type and create an array of different images

### Features:
  1. Re-create any image'(s) with your fingertips
  2. Select different brush types to create beautiful paintings
  3. Ability to save any painting and re-create it or share with friends
  4. Ability to take your own picture and re-create it
  5. Download our stock images to draw!
  
  
# How To Use It
##### Download or clone the project, and open project in android studios. Once loaded select run to load app onto emulator or phone.

  * Once the application is ran on emulator or phone, you can download our stock images our upload your own. 
  * Once you load a picture from the gallery onto the left side of the canvas, you can begin to draw on the right
  * You will notice that the default brush type is square, by tapping the pencil icon you can change your brush types
  * Using the "X" icon, you can clear your canvas and start a fresh

```
  To chnage the radius/size of objects by using the user velocity of their finger use the following code below.
  By changing _defaultRadius with "radius".

```

``` Java
                    float xSpeed = velocityTracker.getXVelocity();
                    float ySpeed = velocityTracker.getYVelocity();

                    double squared = Math.pow(xSpeed, 2) + Math.pow(ySpeed, 2);
                    double halved = squared / 2.0;
                    double final_sum = halved / 1000;

                    if(final_sum > 200.0) {
                        final_sum = 200.0;
                    } else if (final_sum < 25.0) {
                        final_sum = 25.0;
                    }
                    float radius = (float) final_sum;
                    
                    PaintPoint paintP = new PaintPoint(tX,tY,radius,_brushType,_paint);
                    _listPaintPoints.add(paintP);

```
# LICENSE

```
The CK Sparc, LLC License

Copyright (c) 2016 CK Sparc, LLC (http://www.cksparc.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
