package com.moodeekey.fmbus.schedule_screen;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.moodeekey.fmbus.R;
import com.moodeekey.fmbus.data_handling.DatabaseAdapter;

import java.io.File;
import java.util.List;

/**
 * Fragment to display bus schedule image on the ScheduleScreen. Enables Pinch zoom in/out
 * capabilities as well as scrolling
 */
public class ImageFragment extends Fragment {
    DisplayMetrics displaymetrics;

    Display display;

    public static final String ARG_ROUTE_NUMBER = "route_number";

    public void ImageFragment() {
        // Empty constructor required for fragment subclasses
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.image_fragment, container, false);
        int i = getArguments().getInt(ARG_ROUTE_NUMBER);

        DatabaseAdapter db;
        db = new DatabaseAdapter(getActivity().getApplicationContext());
        db.createDatabase();
        db.open();
        List<String> route_list = db.getAllRouteCodes();//codes used to locate image files
        db.close();

        String route = route_list.get(i);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.fmbus";
        File file = new File(path +"/" + route + ".jpg");
        Bitmap imageId = BitmapFactory.decodeFile(file.getAbsolutePath());

        final ImageView schedule_jpg = ((ImageView) rootView.findViewById(R.id.image));

        //-----------
        displaymetrics = new DisplayMetrics();
        display = getActivity().getWindowManager().getDefaultDisplay();
        display.getMetrics(displaymetrics);

        //--------------------
        schedule_jpg.setImageBitmap(imageId);
        schedule_jpg.setOnTouchListener(new View.OnTouchListener() {

            String TAG = "Zoom class";
            // These matrices will be used to move and zoom image
            Matrix matrix = new Matrix();
            Matrix savedMatrix = new Matrix();

            // We can be in one of these 3 states
            static final int NONE = 0;
            static final int DRAG = 1;
            static final int ZOOM = 2;
            int mode = NONE;

            // Remember some things for zooming
            PointF start = new PointF();
            PointF mid = new PointF();
            float oldDist = 1f;


            public boolean onTouch(View v, MotionEvent event)
            {

                ImageView view = (ImageView) v;
                view.setScaleType(ImageView.ScaleType.MATRIX);
                float scale;

                // Handle touch events here...

                switch (event.getAction() & MotionEvent.ACTION_MASK)
                {
                    case MotionEvent.ACTION_DOWN:   // first finger down only
                        savedMatrix.set(matrix);
                        start.set(event.getX(), event.getY());

                        mode = DRAG;
                        break;

                    case MotionEvent.ACTION_UP: // first finger lifted

                    case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                        mode = NONE;

                        break;

                    case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                        oldDist = spacing(event);

                        if (oldDist > 5f) {
                            savedMatrix.set(matrix);
                            midPoint(mid, event);
                            mode = ZOOM;

                        }
                        break;

                    case MotionEvent.ACTION_MOVE:

                        if (mode == DRAG)
                        {

                            matrix.set(savedMatrix);

                            Matrix humatrix = schedule_jpg.getImageMatrix();
                            float[] values = new float[9];
                            humatrix.getValues(values);
                            float newX = event.getX() - start.x;
                            float newY = event.getY() - start.y;
                            matrix.postTranslate(newX, newY);

                            // create the transformation in the matrix  of points
                        }
                        else if (mode == ZOOM)
                        {
                            // pinch zooming
                            float newDist = spacing(event);

                            if (newDist > 10f)
                            {
                                matrix.set(savedMatrix);
                                scale = newDist / oldDist; // setting the scaling of the
                                // matrix...if scale > 1 means
                                // zoom in...if scale < 1 means
                                // zoom out

                                matrix.postScale(scale, scale, mid.x, mid.y);
                            }
                        }

                        break;
                }
                view.setImageMatrix(matrix); // display the transformation on screen
                return true; // indicate event was handled
            }

    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

            private float spacing(MotionEvent event)
            {
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                return FloatMath.sqrt(x * x + y * y);
            }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

            private void midPoint(PointF point, MotionEvent event)
            {
                float x = event.getX(0) + event.getX(1);
                float y = event.getY(0) + event.getY(1);
                point.set(x / 2, y / 2);
            }

        });

        return rootView;
    }

}
