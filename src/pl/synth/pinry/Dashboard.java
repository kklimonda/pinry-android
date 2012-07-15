package pl.synth.pinry;

import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.widget.ListView;

public class Dashboard extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dashboard);
    }
}
