package com.alar.cellowar.client;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.alar.cellowar.R;
import com.alar.cellowar.shared.datatypes.Antenna;
import com.alar.cellowar.shared.datatypes.CelloWarGameData;
import com.alar.cellowar.shared.datatypes.Obstacle;

/**
 * Created by alexi on 1/26/2018.
 */

class GameView extends View {

    CelloWarGameData _m;
    int _my_base_id;

    //Rect r = new Rect(0,0, 100, 100);
    //Rect temp_r = new Rect(r);
    Antenna dragging = null;
    float original_x;
    float original_y;
    float dx;
    float dy;
    Paint p;
    //BitmapShader bmpShader;
    //Bitmap patternBMP;

    Drawable antennaIcon = getResources().getDrawable(R.drawable.ant);

    public GameView (Context context, AttributeSet attrs) {
        super(context, attrs);

        p = new Paint();

        //patternBMP = BitmapFactory.decodeResource(getResources(), R.drawable.);
        //bmpShader = new BitmapShader(patternBMP,
        //        Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        _m = new CelloWarGameData();
        _my_base_id = 1;

        _m.CalcRouting(this.getWidth(), this.getHeight());

    }

    // TODO: !
    public GameView (Context context, CelloWarGameData m, int my_base_id) {
        super(context);
        p = new Paint();
        _m = m;
        _my_base_id = my_base_id;



        _m.CalcRouting(this.getWidth(), this.getHeight());
    }

    public void setMap(CelloWarGameData m) {
        _m = m;
        _m.CalcRouting(this.getWidth(), this.getHeight());
        invalidate();
    }

    public CelloWarGameData getMap() {
        return _m;
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawRGB(60, 80, 60);
        p.reset();

        // Draw Bases
        //
        p.setAlpha(0);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.RED);
        canvas.drawRect(0.0f,
                this.getHeight() - CelloWarGameData.BASE_H,
                this.getWidth() / 2.0f,
                (float)this.getHeight(),
                p);
        canvas.drawRect(this.getWidth() / 2.0f,
                0.0f,
                (float)this.getWidth(),
                CelloWarGameData.BASE_H,
                p);

        p.setColor(Color.BLUE);
        canvas.drawRect(this.getWidth() / 2.0f,
                this.getHeight() - CelloWarGameData.BASE_H,
                (float)this.getWidth(),
                (float)this.getHeight(),
                p);
        canvas.drawRect(0.0f,
                0.0f,
                this.getWidth() / 2.0f,
                CelloWarGameData.BASE_H,
                p);

        // Draw Obstacles
        //
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(40,60,40));
        for (Obstacle o : _m.obst) {
            canvas.drawRect(o._left, o._top, o._right, o._bottom, p);
        }

        // Draw your Antennas
        //
        for (Antenna a : _m.ants) {
            float this_dx = 0;
            float this_dy = 0;

            if (dragging == a) {
                this_dx = dx;
                this_dy = dy;
            }

            p.setColor(Color.BLUE);
            antennaIcon.setBounds(
                    (int)(a.getLeft()   + this_dx),
                    (int)(a.getTop()    + this_dy),
                    (int)(a.getRight()  + this_dx),
                    (int)(a.getBottom() + this_dy)
            );

            //antennaIcon.setColorFilter( 0xffff0000, PorterDuff.Mode.MULTIPLY );

            antennaIcon.draw(canvas);

            if (a._type == Antenna.AntennaType.TRANSMISSION) {

               boolean blue_route =
                        a.routing.routed_bases_top.contains(1) ||
                        a.routing.routed_bases_bottom.contains(1);

                boolean red_route =
                        a.routing.routed_bases_top.contains(2) ||
                        a.routing.routed_bases_bottom.contains(2);

                if(blue_route && red_route) {
                    p.setColor(Color.rgb(150, 0, 150));
                    antennaIcon.setColorFilter(Color.rgb(150, 0, 150), PorterDuff.Mode.MULTIPLY);
                } else if (blue_route) {
                    p.setColor(Color.BLUE);
                    antennaIcon.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
                } else if (red_route ) {
                    p.setColor(Color.RED);
                    antennaIcon.setColorFilter( Color.RED, PorterDuff.Mode.MULTIPLY );
                } else {
                    p.setColor(Color.LTGRAY);
                    antennaIcon.setColorFilter( Color.WHITE, PorterDuff.Mode.MULTIPLY );
                }

                antennaIcon.draw(canvas);
                p.setAlpha(50);
                canvas.drawCircle(a._x + this_dx, a._y + this_dy, a._radius, p);
            } else if (a._type == Antenna.AntennaType.ELECTONIC_WARFARE) {
                antennaIcon.setColorFilter( Color.WHITE, PorterDuff.Mode.MULTIPLY );
                antennaIcon.draw(canvas);

                p.setStyle(Paint.Style.FILL);
                //PorterDuff.Mode mode = PorterDuff.Mode.DST_OUT;
                //p.setXfermode(new PorterDuffXfermode((mode)));

                p.setColor(Color.DKGRAY);
                p.setAlpha(40);
                canvas.drawCircle(a._x + this_dx, a._y + this_dy, a._radius, p);

                p.setStyle(Paint.Style.STROKE);
                p.setColor(Color.WHITE);
                canvas.drawCircle(a._x + this_dx, a._y + this_dy, a._radius, p);
            }
                /*p.setAlpha(80);
                p.setColor(Color.GREEN);
                p.setStyle(Paint.Style.FILL);
                Rect rr = a.getCollisionRect(this_dx, this_dy);
                canvas.drawRect(rr, p);*/
        }

        invalidate();
    }

    public boolean onTouchEvent(final MotionEvent event) {
        boolean handled = false;

        if(_m.state == CelloWarGameData.State.ANT_PLACEMENT) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:

                    for (Antenna a : _m.ants) {
                        //Rect r = a.getRect();
                        //if (r.contains((int) event.getX(), (int) event.getY())) {
                         if(a.isInsideHalo(event.getX(), event.getY())) {
                            original_x = event.getX();
                            original_y = event.getY();
                            dragging = a;
                            break;
                        }
                    }

                    handled = true;
                    break;


                case MotionEvent.ACTION_UP:

                    if (dragging != null) {
                        boolean intersection = false;
                        for (Obstacle o : _m.obst) {

                            if (Rect.intersects(
                                    dragging.getCollisionRect(dx, dy),

                                    new Rect(
                                            (int) o._left,
                                            (int) o._top,
                                            (int) o._right,
                                            (int) o._bottom))) {
                                intersection = true;
                            }
                        }
                        if (intersection == false) {
                            dragging._x = dragging._x + dx;
                            dragging._y = dragging._y + dy;

                            _m.CalcRouting(this.getWidth(), this.getHeight());
                        }
                    }
                    dx = 0.0f;
                    dy = 0.0f;
                    dragging = null;
                    handled = true;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (dragging != null) {
                        dx = event.getX() - original_x;
                        dy = event.getY() - original_y;
                    }
                    handled = true;
                    break;
            }
        }
        return super.onTouchEvent(event) || handled;
    }
}