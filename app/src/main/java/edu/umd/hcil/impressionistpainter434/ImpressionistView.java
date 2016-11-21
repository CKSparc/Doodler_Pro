package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import static android.R.attr.x;
import static android.R.attr.y;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private ImageView _imageView;

    VelocityTracker velocityTracker = null;

    private ArrayList<PaintPoint> _listPaintPoints = new ArrayList<PaintPoint>();

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private static Paint _paint = new Paint();

    private int _alpha = 150;
    private float _defaultRadius = 50f;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);
        this.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }


    public void save(){
        if(_offScreenCanvas != null) {
            Bitmap bitmap = _offScreenBitmap;
            String path = getContext().getFilesDir().getAbsolutePath();
            File file = new File(path + "/image.png");
            FileOutputStream ostream;
            try {
                file.createNewFile();
                ostream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                ostream.flush();
                ostream.close();
                MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, "path" , "");
                Toast.makeText(getContext().getApplicationContext(), "Painting saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext().getApplicationContext(), "Save Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        //TODO

        if(_offScreenCanvas != null) {
            Paint _p = new Paint();
            _p.setColor(Color.WHITE);
            _p.setStyle(Paint.Style.FILL);
            _offScreenCanvas.drawRect(0,0,this.getWidth(),this.getHeight(),_p);
            _paint = new Paint();

            _listPaintPoints.clear();

            _paint.setColor(Color.RED);
            _paint.setAlpha(_alpha);
            _paint.setAntiAlias(true);
            _paint.setStyle(Paint.Style.FILL);
            _paint.setStrokeWidth(4);
        }

        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        //TODO
        //Basically, the way this works is to list for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location

        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                velocityTracker.addMovement(motionEvent);

                break;
            case MotionEvent.ACTION_MOVE:

                velocityTracker.addMovement(motionEvent);
                velocityTracker.computeCurrentVelocity(1000);
                //1000 provides pixels per second


                int history_size = motionEvent.getHistorySize();
                for(int i = 0; i < history_size; i++) {

//                    float xVelocity = velocityTracker.getXVelocity();
//                    float yVelocity = velocityTracker.getYVelocity();
//
//                    double squared = Math.pow(xVelocity, 2) + Math.pow(yVelocity, 2);
//                    double halved = squared / 2.0;
//                    double final_sum = halved / 1000;
//
//                    if(final_sum > 200.0) {
//                        final_sum = 200.0;
//                    } else if (final_sum < 25.0) {
//                        final_sum = 25.0;
//                    }
//                    float radius = (float) final_sum;

                    float tX = motionEvent.getHistoricalX(i);
                    float tY = motionEvent.getHistoricalY(i);

                    int color = getPixelColor((int)tX,(int)tY);
                    _paint.setColor(color);

                    PaintPoint paintP = new PaintPoint(tX,tY,_defaultRadius,_brushType,_paint);
                    _listPaintPoints.add(paintP);
                }

//                float xVelocity = velocityTracker.getXVelocity();
//                float yVelocity = velocityTracker.getYVelocity();
//
//                double squared = Math.pow(xVelocity, 2) + Math.pow(yVelocity, 2);
//                double halved = squared / 2.0;
//                double final_sum = halved / 1000;
//
//                if(final_sum > 200.0) {
//                    final_sum = 200.0;
//                } else if (final_sum < 25.0) {
//                    final_sum = 25.0;
//                }
//                float radius = (float) final_sum;

                float x = motionEvent.getX();
                float y = motionEvent.getY();

                int color = getPixelColor((int)x,(int)y);
                _paint.setColor(color);

                PaintPoint paintP = new PaintPoint(x,y,_defaultRadius,_brushType,_paint);
                _listPaintPoints.add(paintP);

                onDrawShape(_listPaintPoints);
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                // Return a VelocityTracker object back to be re-used by others.
                velocityTracker.recycle();
                break;
        }

        invalidate();
        return true;
    }

    private int getPixelColor(int touchX,int touchY) {

        if(_imageView == null) return Color.WHITE;

        Rect rec = getBitmapPositionInsideImageView(_imageView);

        if(!rec.contains(touchX,touchY)) return Color.WHITE;

        if(!_imageView.getDrawable().getBounds().contains(touchX,touchY)) return Color.WHITE;

        Bitmap bitmap = ((BitmapDrawable)_imageView.getDrawable()).getBitmap();

        if(touchY > bitmap.getHeight() || touchX > bitmap.getWidth()) {
            return Color.WHITE;
        }

        if (touchX < 0) touchX = 0;

        if (touchY < 0) touchY = 0;


        int pixel = bitmap.getPixel(touchX,touchY);

        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);

        return Color.rgb(redValue,greenValue,blueValue);
    }

    private void onDrawShape(ArrayList<PaintPoint> list){
        Random _random = new Random();

        for(PaintPoint pp : list) {

            switch (pp.getBrushType()) {
                case Circle:
                    _offScreenCanvas.drawCircle(pp.getX(), pp.getY(), pp.getBrushRadius(), pp.getPaint());
                    break;
                case Square:
                    _offScreenCanvas.drawRect(pp.getX(),pp.getY(),pp.getX() + pp.getBrushRadius(), pp.getY() + pp.getBrushRadius(), pp.getPaint());
                    break;
                case Line:
                    _offScreenCanvas.drawLine(pp.getX(), pp.getY(),pp.getX()+pp.getBrushRadius(), pp.getY()+pp.getBrushRadius(),pp.getPaint());
                    break;
                case LineSplatter:
                    // One circle
                    float xL = pp.getX() + (_random.nextFloat() * 100f);
                    float yL = pp.getY() + (_random.nextFloat() * 100f);

                    pp.getPaint().setColor(getPixelColor((int) xL,(int) yL));
                    _offScreenCanvas.drawLine(xL, yL, xL + pp.getBrushRadius(), yL + pp.getBrushRadius(),pp.getPaint());

                    // Two circle
                    xL = pp.getX() - (_random.nextFloat() * 100f);
                    yL = pp.getY() - (_random.nextFloat() * 100f);

                    pp.getPaint().setColor(getPixelColor((int) xL,(int) yL));

                    _offScreenCanvas.drawLine(xL, yL, xL + pp.getBrushRadius(), yL + pp.getBrushRadius(),pp.getPaint());

                    // Three circle
                    xL = pp.getX() + (_random.nextFloat() * 100f);
                    yL = pp.getY() - (_random.nextFloat() * 100f);

                    pp.getPaint().setColor(getPixelColor((int) xL,(int) yL));

                    _offScreenCanvas.drawLine(xL, yL, xL + pp.getBrushRadius(), yL + pp.getBrushRadius(),pp.getPaint());

                    // Four circle
                    xL = pp.getX() - (_random.nextFloat() * 100f);
                    yL = pp.getY() + (_random.nextFloat() * 100f);

                    pp.getPaint().setColor(getPixelColor((int) xL,(int) yL));

                    _offScreenCanvas.drawLine(xL, yL, xL + pp.getBrushRadius(), yL + pp.getBrushRadius(),pp.getPaint());
                    break;
                case CircleSplatter:
                    // One circle
                    float x = pp.getX() + (_random.nextFloat() * 100f);
                    float y = pp.getY() + (_random.nextFloat() * 100f);

                    pp.getPaint().setColor(getPixelColor((int) x,(int) y));
                    _offScreenCanvas.drawCircle(x,y,pp.getBrushRadius() * _random.nextFloat(),pp.getPaint());

                    // Two circle
                     x = pp.getX() - (_random.nextFloat() * 100f);
                     y = pp.getY() - (_random.nextFloat() * 100f);

                    pp.getPaint().setColor(getPixelColor((int) x,(int) y));

                    _offScreenCanvas.drawCircle(x,y,pp.getBrushRadius() * _random.nextFloat(),pp.getPaint());

                    // Three circle
                    x = pp.getX() + (_random.nextFloat() * 100f);
                    y = pp.getY() - (_random.nextFloat() * 100f);

                    pp.getPaint().setColor(getPixelColor((int) x,(int) y));

                    _offScreenCanvas.drawCircle(x,y,pp.getBrushRadius() * _random.nextFloat(),pp.getPaint());

                    // Four circle
                    x = pp.getX() - (_random.nextFloat() * 100f);
                    y = pp.getY() + (_random.nextFloat() * 100f);

                    pp.getPaint().setColor(getPixelColor((int) x,(int) y));

                    _offScreenCanvas.drawCircle(x,y,pp.getBrushRadius() * _random.nextFloat(),pp.getPaint());
                    break;
                default:
                    break;
            }
        }

    }


    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }

    private class PaintPoint {
        private Paint _paint = new Paint();
        private PointF _point;
        private float _brushRadius;
        private BrushType _brushType;

        public PaintPoint(float x, float y, float brushRadius, BrushType brushType, Paint paintSrc){
            // Copy the fields from paintSrc into this paint
            _paint.set(paintSrc);
            _point = new PointF(x, y);
            _brushRadius = brushRadius;
            _brushType = brushType;
        }

        public Paint getPaint(){
            return _paint;
        }

        public float getX(){
            return _point.x;
        }

        public float getY(){
            return _point.y;
        }

        public float getBrushRadius(){
            return _brushRadius;
        }

        public BrushType getBrushType(){ return _brushType; }
    }

}

