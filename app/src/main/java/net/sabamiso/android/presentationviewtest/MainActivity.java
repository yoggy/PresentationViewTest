/**
 * see also...
 *   Presentation | Android Developers
 *   http://developer.android.com/reference/android/app/Presentation.html
 */
package net.sabamiso.android.presentationviewtest;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaRouter;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    private MediaRouter mediaRouter;
    private TestPresentation presentation;
    private boolean paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
    }

    @Override
    protected void onResume() {
        paused = false;

        Log.d(TAG, "onResume()");
        super.onResume();

        mediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
        updatePresentation();
    }

    @Override
    protected void onPause() {
        paused = true;

        Log.d(TAG, "onPause()");
        super.onPause();

        mediaRouter.removeCallback(mMediaRouterCallback);
        //updatePresentation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (presentation != null) {
            presentation.dismiss();
            presentation = null;
        }
    }

    private void updatePresentation() {
        MediaRouter.RouteInfo route = mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);

        Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;

        if (presentation != null && presentation.getDisplay() != presentationDisplay) {
            Log.i(TAG, "Dismissing presentation because the current route no longer has a presentation display.");
            presentation.dismiss();
            presentation = null;
        }

        if (presentation == null && presentationDisplay != null) {
            Log.i(TAG, "Showing presentation on display: " + presentationDisplay);
            presentation = new TestPresentation(this, presentationDisplay);
            presentation.setOnDismissListener(mOnDismissListener);
            try {
                presentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                Log.w(TAG, "Couldn't show presentation!  Display was removed in the meantime.", ex);
                presentation = null;
            }
        }
    }

    private final MediaRouter.SimpleCallback mMediaRouterCallback =
            new MediaRouter.SimpleCallback() {
                @Override
                public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRouteSelected: type=" + type + ", info=" + info);
                    updatePresentation();
                }

                @Override
                public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRouteUnselected: type=" + type + ", info=" + info);
                    updatePresentation();
                }

                @Override
                public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRoutePresentationDisplayChanged: info=" + info);
                    updatePresentation();
                }
            };

    private final DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (dialog == presentation) {
                        Log.i(TAG, "Presentation was dismissed.");
                        presentation = null;
                    }
                }
            };

    private final static class TestPresentation extends Presentation {
        public TestPresentation(Context context, Display display) {
            super(context, display);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_presentation);
        }
    }
}
