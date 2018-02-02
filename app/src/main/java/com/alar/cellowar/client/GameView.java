package com.alar.cellowar.client;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
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

import java.util.HashSet;
import java.util.Set;

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

    BitmapShader bmpShader;
    Bitmap patternBMP;
    Drawable antennaIcon = getResources().getDrawable(R.drawable.ant);
    Drawable ewIcon = getResources().getDrawable(R.drawable.anti);
    Drawable goalIcon = getResources().getDrawable(R.drawable.flag);

    Paint pObst;
    Paint pAntNeutralHalo;
    Paint pAntRedHalo;
    Paint pAntBlueHalo;
    Paint pAndBothHalo;
    Paint pAntSpoofedHalo;
    Paint pAntElecWarHaloPart1;
    Paint pAntElecWarHaloPart2;
    Paint pGoal;
    Paint pGoalStroke;

    Paint pBaseRed;
    Paint pBaseBlue;

    public GameView (Context context, AttributeSet attrs) {
        super(context, attrs);

        setupPaint();
        //setMap(new CelloWarGameData());
       // _my_base_id = 0;
    }

    // TODO: ?
    /*
    public GameView (Context context, CelloWarGameData m, int my_base_id) {
        super(context);
        setupPaint()
        _m = m;
        _my_base_id = my_base_id;

        _m.CalcRouting(this.getWidth(), this.getHeight());
    }*/

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (_m != null) {
            _m.UpdateViewSize(w,h);
        }

        invalidate();
    }

    private void setupPaint() {
        pObst = new Paint();
        pObst.setStyle(Paint.Style.FILL);
        pObst.setColor(getResources().getColor(R.color.color_obstacle));

        pAntNeutralHalo = new Paint();
        pAntNeutralHalo.setStyle(Paint.Style.FILL);
        pAntNeutralHalo.setColor(getResources().getColor(R.color.color_ant_halo_default ));
//        pAntNeutralHalo.setAlpha(50);


        pAntRedHalo = new Paint();
        pAntRedHalo.setStyle(Paint.Style.FILL);
        pAntRedHalo.setColor(getResources().getColor(R.color.color_ant_halo_enemy));
//        pAntRedHalo.setAlpha(50);


        pAntBlueHalo = new Paint();
        pAntBlueHalo.setStyle(Paint.Style.FILL);
        pAntBlueHalo.setColor(getResources().getColor(R.color.color_ant_halo_friendly));
//        pAntBlueHalo.setAlpha(50);


        // Do not use the shader paint - too slow
        //patternBMP = BitmapFactory.decodeResource(getResources(), R.drawable.checker);
        //bmpShader = new BitmapShader(patternBMP,
        //        Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        pAndBothHalo = new Paint();
        pAndBothHalo.setStyle(Paint.Style.FILL);
        pAndBothHalo.setColor(getResources().getColor(R.color.color_ant_halo_both));
//        pAndBothHalo.setAlpha(50);
        //pAndBothHalo.setColor(0xffffff);
        //pAndBothHalo.setShader(bmpShader);

        pAntSpoofedHalo = new Paint();
        pAntSpoofedHalo.setStyle(Paint.Style.FILL);
        pAntSpoofedHalo.setColor(getResources().getColor(R.color.color_ant_halo_spoofed));
//        pAntSpoofedHalo.setAlpha(50);


        pAntElecWarHaloPart1 = new Paint();
        pAntElecWarHaloPart1.setStyle(Paint.Style.FILL);
        pAntElecWarHaloPart1.setColor(getResources().getColor(R.color.color_anti_halo));
//        pAntElecWarHaloPart1.setAlpha(40);

        //PorterDuff.Mode mode = PorterDuff.Mode.DST_OUT;
        //p.setXfermode(new PorterDuffXfermode((mode)));

        pAntElecWarHaloPart2 = new Paint();
        pAntElecWarHaloPart2.setStyle(Paint.Style.STROKE);
        pAntElecWarHaloPart2.setColor(getResources().getColor(R.color.color_anti_halo_stroke));

        pBaseRed = new Paint();
        pBaseRed.setStyle(Paint.Style.FILL);
        pBaseRed.setColor(getResources().getColor(R.color.color_enemy_base));

        pBaseBlue = new Paint();
        pBaseBlue.setStyle(Paint.Style.FILL);
        pBaseBlue.setColor(getResources().getColor(R.color.color_friendly_base));

        pGoal = new Paint();
        pGoal.setStyle((Paint.Style.FILL));
        pGoal.setColor(getResources().getColor(R.color.color_goal));

        pGoalStroke = new Paint();
        pGoalStroke.setStyle(Paint.Style.STROKE);
        pGoalStroke.setColor(getResources().getColor(R.color.color_goal_stroke));

    }

    public void setMap(CelloWarGameData m) {


        /***
         * ARTIUMS DEBUG DATA
         */
         /*m.ants.clear();
         m.obst.clear();

         m.UpdateViewSize(100.0f, 100.0f);

         m.obst.add(new Obstacle(20.0f, 20.0f, 30.0f, 30.0f));
         m.obst.add(new Obstacle(70.0f, 70.0f, 80.0f, 80.0f));
         m.obst.add(new Obstacle(70.0f, 20.0f, 80.0f, 30.0f));
         m.obst.add(new Obstacle(20.0f, 70.0f, 30.0f, 80.0f));

         m.obst.add(new Obstacle(25.0f, 45.0f, 35.0f, 55.0f));
         m.obst.add(new Obstacle(65.0f, 45.0f, 75.0f, 55.0f));

        m.obst.add(new Obstacle(90.0f, 90.0f, 100.0f, 100.0f));

        m.obst.add(new Obstacle(0.0f, 90.0f, 10.0f, 100.0f));
        m.obst.add(new Obstacle(90.0f, 0.0f, 100.0f, 10.0f));

       m.ants.add(new Antenna(30.0f, 10.0f, 10.0f, Antenna.AntennaType.TRANSMISSION ));
       m.ants.add(new Antenna(15.0f, 15.0f, 10.0f, Antenna.AntennaType.TRANSMISSION ));
       m.ants.add(new Antenna(15.0f, 20.0f, 10.0f, Antenna.AntennaType.TRANSMISSION ));
       m.ants.add(new Antenna(15.0f, 25.0f, 10.0f, Antenna.AntennaType.TRANSMISSION ));
       m.ants.add(new Antenna(15.0f, 10.0f, 15.0f, Antenna.AntennaType.TRANSMISSION ));
       m.ants.add(new Antenna(10.0f, 10.0f, 20.0f, Antenna.AntennaType.ELECTONIC_WARFARE ));
       m.ants.add(new Antenna(10.0f, 10.0f, 25.0f, Antenna.AntennaType.ELECTONIC_WARFARE ));*/

        _m = m;

        if(getWidth() != 0.0f && getHeight() != 0.0f) {
            _m.UpdateViewSize(this.getWidth(), getHeight());
        }
        invalidate();
    }

    public CelloWarGameData getMap() {
        return _m;
    }

    public int getMyBaseId() {
        return _my_base_id;
    }

    public void setMyBaseId(int id) {
        _my_base_id = id;
    }

    /*public Set<Integer> DetermineInterconnectedBases() {
        HashSet<Integer> ret = new HashSet<>();
        for(Antenna a : _m.ants) {
            if (a._type == Antenna.AntennaType.TRANSMISSION) {
                Set<Integer> intersection = new HashSet<Integer>(a.routing.routed_bases_top);
                intersection.retainAll(a.routing.routed_bases_bottom);
                ret.addAll(intersection);
            }
        }
        return ret;
    }*/
    public Set<Integer> DetermineInterconnectedBases() {
        HashSet<Integer> ret = new HashSet<>();
        for (Antenna a : _m.ants) {
            if (a._type == Antenna.AntennaType.TRANSMISSION) {
                if (a.routing.routed_goal) {
                    ret.addAll(a.routing.routed_bases_top);
                    ret.addAll(a.routing.routed_bases_bottom);
                }
            }
        }
        return ret;
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawColor(getResources().getColor(R.color.color_game_background));

        // Draw Bases
        //
        canvas.drawRect(0.0f,
                this.getHeight() - CelloWarGameData.BASE_H,
                this.getWidth() / 2.0f,
                (float)this.getHeight(),
                pBaseRed);
        canvas.drawRect(this.getWidth() / 2.0f,
                0.0f,
                (float)this.getWidth(),
                CelloWarGameData.BASE_H,
                pBaseRed);

        canvas.drawRect(this.getWidth() / 2.0f,
                this.getHeight() - CelloWarGameData.BASE_H,
                (float)this.getWidth(),
                (float)this.getHeight(),
                pBaseBlue);
        canvas.drawRect(0.0f,
                0.0f,
                this.getWidth() / 2.0f,
                CelloWarGameData.BASE_H,
                pBaseBlue);

        // Draw Obstacles
        //
        for (Obstacle o : _m.obst) {
            canvas.drawRect(o._left, o._top, o._right, o._bottom, pObst);
        }

        // Draw Goal
        float goal_a = (float)(Math.sqrt(2) *  _m.GOAL_RADIUS);
        goalIcon.setBounds(
                (int)(this.getWidth() / 2  - goal_a / 2),
                (int)(this.getHeight() / 2 - goal_a / 2),
                (int)(this.getWidth() / 2  + goal_a / 2),
                (int)(this.getHeight() / 2 + goal_a / 2));

        canvas.drawCircle(this.getWidth() / 2, this.getHeight() / 2, _m.GOAL_RADIUS, pGoal);
        canvas.drawCircle(this.getWidth() / 2, this.getHeight() / 2, _m.GOAL_RADIUS, pGoalStroke);
        goalIcon.draw(canvas);


        // Draw your Antennas
        //
        for (Antenna a : _m.ants) {
            float this_dx = 0;
            float this_dy = 0;

            if (dragging == a) {
                this_dx = dx;
                this_dy = dy;
            }

            antennaIcon.setBounds(
                    (int)(a.getLeft()   + this_dx),
                    (int)(a.getTop()    + this_dy),
                    (int)(a.getRight()  + this_dx),
                    (int)(a.getBottom() + this_dy)
            );

            ewIcon.setBounds(
                    (int)(a.getLeft()   + this_dx),
                    (int)(a.getTop()    + this_dy),
                    (int)(a.getRight()  + this_dx),
                    (int)(a.getBottom() + this_dy)
            );


            if (a._type == Antenna.AntennaType.TRANSMISSION) {

                boolean blue_route =
                        a.routing.routed_bases_top.contains(1) ||
                                a.routing.routed_bases_bottom.contains(1);

                boolean red_route =
                        a.routing.routed_bases_top.contains(2) ||
                                a.routing.routed_bases_bottom.contains(2);

                Paint selectedAntPaint = pAntNeutralHalo;
                int antColorMult = Color.WHITE;

                if (a.routing.isSpoofed) {
                    selectedAntPaint = pAntSpoofedHalo;
                    antColorMult = getResources().getColor(R.color.color_ant_spoofed);
                    //antennaIcon.setColorFilter( Color.DKGRAY, PorterDuff.Mode.MULTIPLY );
                } else if(blue_route && red_route) {
                    selectedAntPaint = pAndBothHalo;
                    antColorMult = getResources().getColor(R.color.color_ant_both);
                    //antennaIcon.setColorFilter(Color.rgb(150, 0, 150), PorterDuff.Mode.MULTIPLY);
                } else if (blue_route) {

                    selectedAntPaint = pAntBlueHalo;
                    antColorMult = getResources().getColor(R.color.color_ant_friendly);
                    //antennaIcon.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
                } else if (red_route ) {
                    selectedAntPaint = pAntRedHalo;
                    antColorMult = getResources().getColor(R.color.color_ant_enemy);
                    //.setColorFilter( Color.RED, PorterDuff.Mode.MULTIPLY );
                } else {
                    selectedAntPaint = pAntNeutralHalo;
                    antColorMult = getResources().getColor(R.color.color_ant_default);
                }

                //TODO wihtout coloring when touching the goal
//                if (a.routing.isSpoofed == false && a.routing.routed_goal) {
//                    antColorMult = pGoal.getColor();
                    //antColorMult = 0xffff00;
//                }

                antennaIcon.setColorFilter( antColorMult, PorterDuff.Mode.MULTIPLY );
                antennaIcon.draw(canvas);
                canvas.drawCircle(a._x + this_dx, a._y + this_dy, a._radius, selectedAntPaint);
            } else if (a._type == Antenna.AntennaType.ELECTONIC_WARFARE) {

                //antennaIcon.setColorFilter( Color.WHITE, PorterDuff.Mode.MULTIPLY );
                ewIcon.draw(canvas);

                canvas.drawCircle(a._x + this_dx, a._y + this_dy, a._radius, pAntElecWarHaloPart1);
                canvas.drawCircle(a._x + this_dx, a._y + this_dy, a._radius, pAntElecWarHaloPart2);
            }
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