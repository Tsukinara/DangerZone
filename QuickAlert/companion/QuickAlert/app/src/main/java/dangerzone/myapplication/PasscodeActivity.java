package dangerzone.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PasscodeActivity extends ActionBarActivity {

    final String UP_CHAR = "\u25B2";
    final String MID_CHAR = "\u25C9";
    final String DOWN_CHAR = "\u25BC";
    final int PASSCODE_LEN = 5;
    final int grey = Color.rgb(238,238,238);
    final int yellow = Color.parseColor("yellow");
    String new_string;
    String confirm_string;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        final TextView current_passcode = (TextView) findViewById(R.id.current_passcode);
        final TextView new_passcode = (TextView) findViewById(R.id.new_passcode);
        final TextView confirm_passcode = (TextView) findViewById(R.id.confirm_passcode);
        new_passcode.setBackgroundColor(yellow);
        Button up_button = (Button) findViewById(R.id.up_button);
        Button mid_button = (Button) findViewById(R.id.mid_button);
        Button down_button = (Button) findViewById(R.id.down_button);
        Button reset_button = (Button) findViewById(R.id.reset_button);
        Button clear_button = (Button) findViewById(R.id.clear_button);

        up_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(new_passcode.getText().length()<PASSCODE_LEN) {
                    new_string += "1";
                    new_passcode.append(UP_CHAR);
                    if (new_passcode.getText().length() == PASSCODE_LEN) {
                        new_passcode.setBackgroundColor(grey);
                        confirm_passcode.setBackgroundColor(yellow);
                    }
                }
                else if(confirm_passcode.getText().length()<PASSCODE_LEN){
                    confirm_string += "1";
                    confirm_passcode.append(UP_CHAR);
                    if (confirm_passcode.getText().length() == PASSCODE_LEN) {
                        confirm_passcode.setBackgroundColor(grey);
                    }
                }
            }
        });
        mid_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(new_passcode.getText().length()<PASSCODE_LEN) {
                    new_string += "2";
                    new_passcode.append(MID_CHAR);
                    if (new_passcode.getText().length() == PASSCODE_LEN) {
                        new_passcode.setBackgroundColor(grey);
                        confirm_passcode.setBackgroundColor(yellow);
                    }
                }
                else if(confirm_passcode.getText().length()<PASSCODE_LEN){
                    confirm_string += "2";
                    confirm_passcode.append(MID_CHAR);
                    if (confirm_passcode.getText().length() == PASSCODE_LEN) {
                        confirm_passcode.setBackgroundColor(grey);
                    }
                }
            }
        });
        down_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(new_passcode.getText().length()<PASSCODE_LEN) {
                    new_string += "3";
                    new_passcode.append(DOWN_CHAR);
                    if (new_passcode.getText().length() == PASSCODE_LEN) {
                        new_passcode.setBackgroundColor(grey);
                        confirm_passcode.setBackgroundColor(yellow);
                    }
                }
                else if(confirm_passcode.getText().length()<PASSCODE_LEN){
                    confirm_string += "3";
                    confirm_passcode.append(DOWN_CHAR);
                    if (confirm_passcode.getText().length() == PASSCODE_LEN) {
                        confirm_passcode.setBackgroundColor(grey);
                    }
                }
            }
        });
        reset_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(new_passcode.getText().length()==PASSCODE_LEN && confirm_passcode.getText().length()==PASSCODE_LEN) {
                    if (!new_string.equals(confirm_string))
                        Toast.makeText(getApplicationContext(), "Mismatch: Human is dumb!", Toast.LENGTH_LONG).show();
                    else {
                        current_passcode.setText(confirm_passcode.getText());
                        Toast.makeText(getApplicationContext(), "Passcode set!", Toast.LENGTH_LONG).show();
                    }
                    new_string = "";
                    confirm_string = "";
                    new_passcode.setText("");
                    new_passcode.setBackgroundColor(yellow);
                    confirm_passcode.setText("");
                }
            }
        });
        clear_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new_string = "";
                confirm_string = "";
                new_passcode.setText("");
                new_passcode.setBackgroundColor(yellow);
                confirm_passcode.setText("");
                confirm_passcode.setBackgroundColor(grey);
            }
        });
    }
}
