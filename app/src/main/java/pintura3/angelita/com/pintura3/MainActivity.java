package pintura3.angelita.com.pintura3;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private RelativeLayout drawingLayout;
    private MyView myView;
    Button red, blue, yellow;
    Paint paint;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myView = new MyView(this);
        drawingLayout = (RelativeLayout) findViewById(R.id.relative_layout);
        drawingLayout.addView(myView);

        red = (Button) findViewById(R.id.btn_red);
        blue = (Button) findViewById(R.id.btn_blue);
        yellow = (Button) findViewById(R.id.btn_yellow);

        red.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                paint.setColor(Color.RED);
            }
        });

        yellow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                paint.setColor(Color.YELLOW);
            }
        });
        blue.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                paint.setColor(Color.BLUE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class MyView extends View {

        private Path path;
        Bitmap mBitmap;
        ProgressDialog pd;
        final Point p1 = new Point();
        Canvas canvas;

        // Bitmap mutableBitmap ;
        public MyView(Context context) {
            super(context);

            paint = new Paint();
            paint.setAntiAlias(true);
            pd = new ProgressDialog(context);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(5f);
            mBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.cartoon).copy(Bitmap.Config.ARGB_8888, true);

            this.path = new Path();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            this.canvas = canvas;
            paint.setColor(Color.GREEN);
            canvas.drawBitmap(mBitmap, 0, 0, paint);

        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    p1.x = (int) x;
                    p1.y = (int) y;
                    final int sourceColor = mBitmap.getPixel((int) x, (int) y);
                    final int targetColor = paint.getColor();
                    new TheTask(mBitmap, p1, sourceColor, targetColor).execute();
                    invalidate();
            }
            return true;
        }

        public void clear() {
            path.reset();
            invalidate();
        }

        public int getCurrentPaintColor() {
            return paint.getColor();
        }

        class TheTask extends AsyncTask<Void, Integer, Void> {

            Bitmap bmp;
            Point pt;
            int replacementColor, targetColor;

            public TheTask(Bitmap bm, Point p, int sc, int tc) {
                this.bmp = bm;
                this.pt = p;
                this.replacementColor = tc;
                this.targetColor = sc;
                pd.setMessage("Filling....");
                pd.show();
            }

            @Override
            protected void onPreExecute() {
                pd.show();

            }

            @Override
            protected void onProgressUpdate(Integer... values) {

            }

            @Override
            protected Void doInBackground(Void... params) {
                //FloodFill f = new FloodFill();
                //f.floodFill(bmp, pt, targetColor, replacementColor);
                QueueLinearFloodFiller filler = new QueueLinearFloodFiller(mBitmap, targetColor, replacementColor);
                filler.setTolerance(10);
                filler.floodFill(pt.x, pt.y);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                pd.dismiss();
                invalidate();
            }
        }
    }

    //http://stackoverflow.com/questions/8070401/android-flood-fill-algorithm/17426163#17426163
   // http://stackoverflow.com/questions/6371999/android-fill-image-with-colors
    //IMPORTANT
   // http://stackoverflow.com/questions/16968412/how-to-use-flood-fill-algorithm-in-android
    public class FloodFill {
        public void floodFill(Bitmap image, Point node, int targetColor,
                              int replacementColor) {
            int width = image.getWidth();
            int height = image.getHeight();
            int target = targetColor;
            int replacement = replacementColor;
            if (target != replacement) {
                Queue<Point> queue = new LinkedList<Point>();
                do {

                    int x = node.x;
                    int y = node.y;
                    while (x > 0 && image.getPixel(x - 1, y) == target) {
                        x--;

                    }
                    boolean spanUp = false;
                    boolean spanDown = false;
                    while (x < width && image.getPixel(x, y) == target) {
                        image.setPixel(x, y, replacement);
                        if (!spanUp && y > 0
                                && image.getPixel(x, y - 1) == target) {
                            queue.add(new Point(x, y - 1));
                            spanUp = true;
                        } else if (spanUp && y > 0
                                && image.getPixel(x, y - 1) != target) {
                            spanUp = false;
                        }
                        if (!spanDown && y < height - 1
                                && image.getPixel(x, y + 1) == target) {
                            queue.add(new Point(x, y + 1));
                            spanDown = true;
                        } else if (spanDown && y < height - 1
                                && image.getPixel(x, y + 1) != target) {
                            spanDown = false;
                        }
                        x++;
                    }
                } while ((node = queue.poll()) != null);
            }
        }
    }



    //Mayor Class Optimize

    public class QueueLinearFloodFiller {

        protected Bitmap image = null;
        protected int[] tolerance = new int[] { 0, 0, 0 };
        protected int width = 0;
        protected int height = 0;
        protected int[] pixels = null;
        protected int fillColor = 0;
        protected int[] startColor = new int[] { 0, 0, 0 };
        protected boolean[] pixelsChecked;
        protected Queue<FloodFillRange> ranges;

        // Construct using an image and a copy will be made to fill into,
        // Construct with BufferedImage and flood fill will write directly to
        // provided BufferedImage
        public QueueLinearFloodFiller(Bitmap img) {
            copyImage(img);
        }

        public QueueLinearFloodFiller(Bitmap img, int targetColor, int newColor) {
            useImage(img);

            setFillColor(newColor);
            setTargetColor(targetColor);
        }

        public void setTargetColor(int targetColor) {
            startColor[0] = Color.red(targetColor);
            startColor[1] = Color.green(targetColor);
            startColor[2] = Color.blue(targetColor);
        }

        public int getFillColor() {
            return fillColor;
        }

        public void setFillColor(int value) {
            fillColor = value;
        }

        public int[] getTolerance() {
            return tolerance;
        }

        public void setTolerance(int[] value) {
            tolerance = value;
        }

        public void setTolerance(int value) {
            tolerance = new int[] { value, value, value };
        }

        public Bitmap getImage() {
            return image;
        }

        public void copyImage(Bitmap img) {
            // Copy data from provided Image to a BufferedImage to write flood fill
            // to, use getImage to retrieve
            // cache data in member variables to decrease overhead of property calls
            width = img.getWidth();
            height = img.getHeight();

            image = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(image);
            canvas.drawBitmap(img, 0, 0, null);

            pixels = new int[width * height];

            image.getPixels(pixels, 0, width, 1, 1, width - 1, height - 1);
        }

        public void useImage(Bitmap img) {
            // Use a pre-existing provided BufferedImage and write directly to it
            // cache data in member variables to decrease overhead of property calls
            width = img.getWidth();
            height = img.getHeight();
            image = img;

            pixels = new int[width * height];

            image.getPixels(pixels, 0, width, 1, 1, width - 1, height - 1);
        }

        protected void prepare() {
            // Called before starting flood-fill
            pixelsChecked = new boolean[pixels.length];
            ranges = new LinkedList<FloodFillRange>();
        }

        // Fills the specified point on the bitmap with the currently selected fill
        // color.
        // int x, int y: The starting coords for the fill
        public void floodFill(int x, int y) {
            // Setup
            prepare();

            if (startColor[0] == 0) {
                // ***Get starting color.
                int startPixel = pixels[(width * y) + x];
                startColor[0] = (startPixel >> 16) & 0xff;
                startColor[1] = (startPixel >> 8) & 0xff;
                startColor[2] = startPixel & 0xff;
            }

            // ***Do first call to floodfill.
            LinearFill(x, y);

            // ***Call floodfill routine while floodfill ranges still exist on the
            // queue
            FloodFillRange range;

            while (ranges.size() > 0) {
                // **Get Next Range Off the Queue
                range = ranges.remove();

                // **Check Above and Below Each Pixel in the Floodfill Range
                int downPxIdx = (width * (range.Y + 1)) + range.startX;
                int upPxIdx = (width * (range.Y - 1)) + range.startX;
                int upY = range.Y - 1;// so we can pass the y coord by ref
                int downY = range.Y + 1;

                for (int i = range.startX; i <= range.endX; i++) {
                    // *Start Fill Upwards
                    // if we're not above the top of the bitmap and the pixel above
                    // this one is within the color tolerance
                    if (range.Y > 0 && (!pixelsChecked[upPxIdx])
                            && CheckPixel(upPxIdx))
                        LinearFill(i, upY);

                    // *Start Fill Downwards
                    // if we're not below the bottom of the bitmap and the pixel
                    // below this one is within the color tolerance
                    if (range.Y < (height - 1) && (!pixelsChecked[downPxIdx])
                            && CheckPixel(downPxIdx))
                        LinearFill(i, downY);

                    downPxIdx++;
                    upPxIdx++;
                }
            }

            image.setPixels(pixels, 0, width, 1, 1, width - 1, height - 1);
        }

        // Finds the furthermost left and right boundaries of the fill area
        // on a given y coordinate, starting from a given x coordinate, filling as
        // it goes.
        // Adds the resulting horizontal range to the queue of floodfill ranges,
        // to be processed in the main loop.

        // int x, int y: The starting coords
        protected void LinearFill(int x, int y) {
            // ***Find Left Edge of Color Area
            int lFillLoc = x; // the location to check/fill on the left
            int pxIdx = (width * y) + x;

            while (true) {
                // **fill with the color
                pixels[pxIdx] = fillColor;

                // **indicate that this pixel has already been checked and filled
                pixelsChecked[pxIdx] = true;

                // **de-increment
                lFillLoc--; // de-increment counter
                pxIdx--; // de-increment pixel index

                // **exit loop if we're at edge of bitmap or color area
                if (lFillLoc < 0 || (pixelsChecked[pxIdx]) || !CheckPixel(pxIdx)) {
                    break;
                }
            }

            lFillLoc++;

            // ***Find Right Edge of Color Area
            int rFillLoc = x; // the location to check/fill on the left

            pxIdx = (width * y) + x;

            while (true) {
                // **fill with the color
                pixels[pxIdx] = fillColor;

                // **indicate that this pixel has already been checked and filled
                pixelsChecked[pxIdx] = true;

                // **increment
                rFillLoc++; // increment counter
                pxIdx++; // increment pixel index

                // **exit loop if we're at edge of bitmap or color area
                if (rFillLoc >= width || pixelsChecked[pxIdx] || !CheckPixel(pxIdx)) {
                    break;
                }
            }

            rFillLoc--;

            // add range to queue
            FloodFillRange r = new FloodFillRange(lFillLoc, rFillLoc, y);

            ranges.offer(r);
        }

        // Sees if a pixel is within the color tolerance range.
        protected boolean CheckPixel(int px) {
            int red = (pixels[px] >>> 16) & 0xff;
            int green = (pixels[px] >>> 8) & 0xff;
            int blue = pixels[px] & 0xff;

            return (red >= (startColor[0] - tolerance[0])
                    && red <= (startColor[0] + tolerance[0])
                    && green >= (startColor[1] - tolerance[1])
                    && green <= (startColor[1] + tolerance[1])
                    && blue >= (startColor[2] - tolerance[2]) && blue <= (startColor[2] + tolerance[2]));
        }

        // Represents a linear range to be filled and branched from.
        protected class FloodFillRange {
            public int startX;
            public int endX;
            public int Y;

            public FloodFillRange(int startX, int endX, int y) {
                this.startX = startX;
                this.endX = endX;
                this.Y = y;
            }
        }
    }
}
