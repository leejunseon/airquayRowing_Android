package com.example.jinny.rowing;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StartingHutActivity extends AppCompatActivity {
    private static final String URL_ADDRESS_TIMER_START = "http://192.168.1.79:8080/airquayRowing/main/setRaceStart";
    private static final String URL_ADDRESS_TIME = "http://13.209.161.83:8080/pastTimeSave.jsp";
    private static final String URL_ADDRESS_GET_RACE_NUMBER = "http://13.209.161.83:8080/getRaceNum.jsp";
    final static int IDLE = 0;
    final static int RUNNING = 1;
    final static int PAUSE = 2;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    TextView raceDate, currentTime, timerTime, raceState, raceNum, confirmConnection;
    Button goButton, twoMinutesButton, data, Finish_Button, Reset_Button;
    long baseTime;
    int tStatus = IDLE;
    int tempNumber = 1;
    String LoadData, pastTime;
    boolean flag;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startinghut);
        raceDate = (TextView) findViewById(R.id.current_date);//날짜
        currentTime = (TextView) findViewById(R.id.current_time);//현재 시간
        timerTime = (TextView) findViewById(R.id.ongoing_time);//Start누르고부터의 시간
        raceState = (TextView) findViewById(R.id.race_state);//~번 경기 옆에 경기 상황
        raceNum = (TextView) findViewById(R.id.race_num);//~번
        goButton = (Button) findViewById(R.id.go_button);//Start
        goButton.setEnabled(false);
        Finish_Button=(Button)findViewById(R.id.Finish_button);//종료
        Finish_Button.setEnabled(false);
        Reset_Button=(Button)findViewById(R.id.Reset_Button);//Reset
        Reset_Button.setEnabled(false);

        twoMinutesButton = (Button) findViewById(R.id.two_minute_button);//2분전
        confirmConnection = (TextView) findViewById(R.id.confirm_connection);//왼쪽상단 초록색 네모

        raceState.setVisibility(View.INVISIBLE);

        showTime();

        data = (Button) findViewById(R.id.sendData);//다음 경기
        data.setEnabled(false);
        data.setOnClickListener(new View.OnClickListener() //다음경기 버튼 눌렀을 때의 event
        {
            public void onClick(View view) {
                try {
                    CustomTask a = new CustomTask();
                    a.setDate(raceDate.getText().toString());
                    a.execute();
                    raceNum.setText(String.valueOf(tempNumber));
                    raceState.setVisibility(View.INVISIBLE);
                    timerTime.setText(getReset());
                    twoMinutesButton.setEnabled(true);
                    goButton.setEnabled(false);
                    Finish_Button.setEnabled(false);
                    data.setEnabled(false);
                } catch (Exception e) {
                    Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                    finish();
                    e.printStackTrace();
                }
            }
        });
    }

    Handler mTimer = new Handler()
    {
        public void handleMessage(Message msg) {
            timerTime.setText(getElapse());
            mTimer.sendEmptyMessage(0);
        }

    };

    protected void onDestroy() {
        mTimer.removeMessages(0);
        super.onDestroy();
    }

    public void mOnClick(View v) //2분전 or Start or 종료 or Reset 버튼 눌렀을 때의 event
    {
        switch (v.getId())
        {
            case R.id.go_button://시작버튼 누를 때
                switch (tStatus)
                {
                    case IDLE://2분전 상태
                        try {
                            TimerCaller b = new TimerCaller();
                            b.setDate("1");
                            b.execute();
                        } catch (Exception e) {
                            Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                            finish();
                            e.printStackTrace();
                        }
                        Toast.makeText(getApplicationContext(), "완료되었습니다.", Toast.LENGTH_LONG).show();
                        raceState.setBackground(ContextCompat.getDrawable(this, R.drawable.ongoing_state_border));
                        raceState.setText(" 경기중 ");
                        Finish_Button.setEnabled(true);
                        twoMinutesButton.setEnabled(false);
                        baseTime = SystemClock.elapsedRealtime();
                        mTimer.sendEmptyMessage(0);
                        goButton.setText(" False ");
                        tStatus = RUNNING;
                        Log.i("IDLE", "IDLE");
                        break;
                    case RUNNING://경기중 상태
                        try {
                            TimerCaller d = new TimerCaller();
                            d.setDate("0");
                            d.execute();
                        } catch (Exception e) {
                            Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                            finish();
                            e.printStackTrace();
                        }
                        raceState.setBackground(ContextCompat.getDrawable(this, R.drawable.end_state_border));
                        raceState.setText(" 대기 ");
                        Reset_Button.setEnabled(true);
                        goButton.setEnabled(false);
                        Finish_Button.setEnabled(false);
                        mTimer.removeMessages(0);
                        goButton.setText(" Start ");
                        tStatus = PAUSE;
                        Log.i("RUNNING", "RUNNING");
                        pastTime=timerTime.getText().toString();
                        String[] timeTemp=pastTime.split(":|[.]");
                        Log.i("asdfasdfasdf",timeTemp[0]+"----"+timeTemp[1]+"asdfasdf"+timeTemp[2]);
                        String[] timeSplit=timeTemp[2].split(".");
                        try {
                            TimeSender e = new TimeSender();
                            e.execute(timeTemp[0],timeTemp[1],timeTemp[2],timeTemp[3]);
                        } catch (Exception e) {
                            Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                            finish();
                            e.printStackTrace();
                        }
                        break;
                    case PAUSE://FalseStart 상태
                        try {
                            TimerCaller c = new TimerCaller();
                            c.setDate("1");
                            c.execute();
                        } catch (Exception e) {
                            Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                            finish();
                            e.printStackTrace();
                        }
                        long now = SystemClock.elapsedRealtime();
                        baseTime = (now);
                        mTimer.sendEmptyMessage(0);
                        raceState.setBackground(ContextCompat.getDrawable(this, R.drawable.ongoing_state_border));
                        raceState.setText(" 경기중 ");
                        goButton.setText(" False ");
                        Reset_Button.setEnabled(false);
                        Finish_Button.setEnabled(true);
                        tStatus = RUNNING;
                        Log.i("PAUSE", "PAUSE");
                        break;
                }
                break;

            case R.id.two_minute_button://2분전버튼 누를 때
                goButton.setEnabled(true);
                raceState.setBackground(ContextCompat.getDrawable(this, R.drawable.two_minutes_state_border));
                raceState.setText(" 2 분전 ");
                raceState.setVisibility(View.VISIBLE);
                break;

            case R.id.Finish_button://종료버튼 누를 때
                try {
                    TimerCaller d = new TimerCaller();
                    d.setDate("0");
                    d.execute();
                } catch (Exception e) {
                    Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                    finish();
                    e.printStackTrace();
                }
                mTimer.removeMessages(0);
                tStatus = IDLE;
                raceState.setBackground(ContextCompat.getDrawable(this, R.drawable.two_minutes_state_border));
                raceState.setText(" 경기종료 ");
                goButton.setText(" Start ");
                goButton.setEnabled(false);
                twoMinutesButton.setEnabled(false);
                Reset_Button.setEnabled(false);
                Finish_Button.setEnabled(true);
                data.setEnabled(true);
                break;


            case R.id.Reset_Button://Reset버튼 누를 때
                timerTime.setText(getReset());
                goButton.setEnabled(true);
        }
    }

    String getElapse()
    {
        long now = SystemClock.elapsedRealtime();
        long ell = now - baseTime;
        String sEll = String.format("%02d:%02d:%02d.%02d", ell/1000/360, ell / 1000 / 60, (ell / 1000) % 60, (ell % 1000) / 10);
        return sEll;
    }

    String getReset()
    {
        String sEll=String.format("%02d:%02d:%02d.%02d",00,00,00,00) ;
        return sEll;
    }

    public void showTime() ////현재 날짜, 시간 Diaplay
    {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                raceDate.setText(dateFormat.format(new Date()));
                currentTime.setText(timeFormat.format(new Date()));
            }
        };
        Runnable task = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    handler.sendEmptyMessage(2);
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    class CustomTask extends AsyncTask<Void, String, Void>
    {
        private String data;
        String sendMsg;

        protected void onPreExecute() {
            pDialog = new ProgressDialog(StartingHutActivity.this);
            pDialog.setMessage("검색중입니다...");
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();
        }

        protected Void doInBackground(Void... param) {
            try {
                URL url = new URL(URL_ADDRESS_GET_RACE_NUMBER);//보낼 jsp 주소
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터 전송
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "raceDate=" + data;//보낼 정보
                osw.write(sendMsg);
                osw.flush();

                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line = "";
                String page = "";

                while ((line = reader.readLine()) != null) {
                    page += line;
                }

                JSONObject sObject = new JSONObject(page);
                JSONArray sArray = sObject.getJSONArray("dataSend");
                sObject = sArray.getJSONObject(0);
                if (sObject.getString("key").equals("ok")) {
                    LoadData = sObject.getString("key");
                    tempNumber++;
                } else
                    run();
                flag = true;
            } catch (MalformedURLException | ProtocolException exception) {
                noConfirm();
                exception.printStackTrace();
                finish();
            } catch (IOException io) {
                noConfirm();
                io.printStackTrace();
            } catch (JSONException e) {
                noConfirm();
                e.printStackTrace();
            }
            return null;

        }

        public void noConfirm() {
            confirmHandler.sendEmptyMessage(0);
        }

        private Handler confirmHandler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(StartingHutActivity.this, "서버가 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                confirmConnection.setBackground(getDrawable(R.drawable.not_connected));
                super.handleMessage(msg);
            }

        };

        private void run() {
            handler.sendEmptyMessage(0);
        }

        private Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(StartingHutActivity.this, "오늘 경기는 끝났습니다.", Toast.LENGTH_LONG).show();
                confirmConnection.setBackground(getDrawable(R.drawable.connection_border));
                super.handleMessage(msg);
            }

        };

        protected void onPostExecute(Void aVoid) {
            pDialog.dismiss();
        }

        private void setDate(String date) {
            this.data = date;
        }
    }

    class TimerCaller extends AsyncTask<Void, Void, Void> //웹으로부터 시간정보 가져오는 AsyncTask
    {
        private String sendMsg;
        private String data;

        protected void onPreExecute() {

            pDialog = new ProgressDialog(StartingHutActivity.this);
            pDialog.setMessage("검색중입니다...");
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();
        }

        protected Void doInBackground(Void... param) {
            try {
                URL url = new URL(URL_ADDRESS_TIMER_START); // URL클래스의 생성자로 주소를 넘겨준다.
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // 해당 주소의 페이지로 접속을 하고, 단일 HTTP 접속을 하기위해 캐스트한다.
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // 요청 헤더를 정의한다.
                conn.setRequestMethod("POST");//데이터 전송(POST방식)
                conn.setDoOutput(true); // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션을 정의한다.
                conn.setDoInput(true); // IputStream으로 응답 헤더와 메시지를 읽어들이겠다는 옵션을 정의한다.
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "raceDate=" + data;//보낼 정보
                osw.write(sendMsg);
                osw.flush();

                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); // 응답받은 메시지의 길이만큼 버퍼를 생성하여 읽어들이고, "UTF-8"로 디코딩해서 읽어들인다
                String line = "";
                String page = "";

                while ((line = reader.readLine()) != null) {
                    page += line;
                }

            } catch (MalformedURLException | ProtocolException exception) {
                noConfirm();
                exception.printStackTrace();
                finish();
            } catch (IOException io) {
                noConfirm();
                io.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            pDialog.dismiss();
        }

        public void noConfirm() {
            confirmHandler.sendEmptyMessage(0);
        }

        private Handler confirmHandler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(StartingHutActivity.this, "서버가 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                confirmConnection.setBackground(getDrawable(R.drawable.not_connected));
                super.handleMessage(msg);
            }

        };

        private void setDate(String date) {
            this.data = date;
        }
    }

    class TimeSender extends AsyncTask<String, Void, Void> //웹으로 시간정보 보내는 AsyncTask
    {
        private String sendMsg;

        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL(URL_ADDRESS_TIME);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터 전송
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "HOUR=" + strings[0]+"&MINUTE="+strings[1]+"&SECOND="+strings[2]+"&MILISECOND="+strings[3];//보낼 정보
                Log.i(sendMsg,sendMsg+"??????????????");
                osw.write(sendMsg);
                osw.flush();

                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line = "";
                String page = "";

                while ((line = reader.readLine()) != null) {
                    page += line;
                }

            } catch (MalformedURLException | ProtocolException exception) {
                noConfirm();
                exception.printStackTrace();
                finish();
            } catch (IOException io) {
                noConfirm();
                io.printStackTrace();
            }
            return null;
        }

        public void noConfirm() {
            confirmHandler.sendEmptyMessage(0);
        }

        private Handler confirmHandler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(StartingHutActivity.this, "서버가 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                confirmConnection.setBackground(getDrawable(R.drawable.not_connected));
                super.handleMessage(msg);
            }

        };
    }
}
