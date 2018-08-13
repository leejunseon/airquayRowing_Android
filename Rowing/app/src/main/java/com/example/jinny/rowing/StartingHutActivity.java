package com.example.jinny.rowing;

import android.annotation.SuppressLint;
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
    private static final String URL_ADDRESS_SET_ONOFF = "http://192.168.254.171:8080/airquayRowing/main/setOnOff";//Onoff 조작 URL
    private static final String URL_ADDRESS_STOPTIME = "http://192.168.254.171:8080/airquayRowing/main/pastTimeSave";//멈춘 랩 시간 (종료, 리셋) 전송 URL
    private static final String URL_ADDRESS_STARTTIME="http://192.168.254.171:8080/airquayRowing/main/startTimeSend";//시작 시간 전송 URL
    private static final String URL_ADDRESS_FINISHTIME="http://192.168.254.171:8080/airquayRowing/main/finishTimeSend";//종료 시간 전송 URL
    private static final String URL_ADDRESS_GET_RACE_NUMBER = "http://192.168.254.171:8080/airquayRowing/main/getRaceNum";//경기 유무 확인
    final static int IDLE = 0;
    final static int RUNNING = 1;
    final static int PAUSE = 2;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    TextView raceDate, currentTime, timerTime, raceState, raceNum, confirmConnection;
    Button goButton, twoMinutesButton, data, Reset_Button;
    long baseTime;
    int tStatus = IDLE;
    int tempNumber = 1;
    String LoadData, pastTime;
    String[] timeTemp,timeSplit;
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
                    getRaceNum RaceNum = new getRaceNum();//다음 경기 조회
                    RaceNum.setData(raceDate.getText().toString());
                    RaceNum.execute();

                    raceState.setVisibility(View.INVISIBLE);
                    timerTime.setText(getReset());
                    twoMinutesButton.setEnabled(true);
                    goButton.setEnabled(false);
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
                    case IDLE://2분전 상태 -> start

                        try {
                            StartTimeSender startTimeSend = new StartTimeSender();
                            startTimeSend.setData(currentTime.getText().toString());
                            startTimeSend.execute();
                        } catch (Exception e) {
                            Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                            finish();
                            e.printStackTrace();
                        }

                        Toast.makeText(getApplicationContext(), "완료되었습니다.", Toast.LENGTH_LONG).show();
                        raceState.setBackground(ContextCompat.getDrawable(this, R.drawable.ongoing_state_border));
                        raceState.setText(" 경기중 ");
                        twoMinutesButton.setEnabled(false);
                        baseTime = SystemClock.elapsedRealtime();
                        mTimer.sendEmptyMessage(0);
                        goButton.setText(" False ");
                        tStatus = RUNNING;
                        Log.i("IDLE", "IDLE");

                        break;

                    case RUNNING://경기중 상태 -> false start

                        try {
                            SetOnOff falseStart = new SetOnOff();
                            falseStart.setData("3");
                            falseStart.execute();
                        } catch (Exception e) {
                            Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                            finish();
                            e.printStackTrace();
                        }

                        pastTime=timerTime.getText().toString();
                        timeTemp=pastTime.split(":|[.]");
                       // Log.i("asdfasdfasdf",timeTemp[0]+"----"+timeTemp[1]+"asdfasdf"+timeTemp[2]);
                        timeSplit=timeTemp[2].split(".");
                        try {
                            StopTimeSender falseTimeSender = new StopTimeSender();
                            falseTimeSender.execute(timeTemp[0],timeTemp[1],timeTemp[2],timeTemp[3]);
                        } catch (Exception e) {
                            Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                            finish();
                            e.printStackTrace();
                        }

                        raceState.setBackground(ContextCompat.getDrawable(this, R.drawable.end_state_border));
                        raceState.setText(" 대기 ");
                        Reset_Button.setEnabled(true);
                        goButton.setEnabled(false);
                        mTimer.removeMessages(0);
                        goButton.setText(" Start ");
                        tStatus = PAUSE;
                        Log.i("RUNNING", "RUNNING");
                        break;

                    case PAUSE://FalseStart 상태 -> restart

                        try {
                            StartTimeSender restartTimeSend = new StartTimeSender();
                            restartTimeSend.setData(currentTime.getText().toString());
                            restartTimeSend.execute();
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
                        tStatus = RUNNING;
                        Log.i("PAUSE", "PAUSE");
                        break;
                }
                break;

            case R.id.two_minute_button://2분전버튼 누를 때

                try {
                    SetOnOff twominutes = new SetOnOff();
                    twominutes.setData("2");
                    twominutes.execute();
                } catch (Exception e) {
                    Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                    finish();
                    e.printStackTrace();
                }

                goButton.setEnabled(true);
                raceState.setBackground(ContextCompat.getDrawable(this, R.drawable.two_minutes_state_border));
                raceState.setText(" 2 분전 ");
                raceState.setVisibility(View.VISIBLE);
                break;

            case R.id.Reset_Button://Reset버튼 누를 때

                try {
                    SetOnOff Reset = new SetOnOff();
                    Reset.setData("4");
                    Reset.execute();
                } catch (Exception e) {
                    Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                    finish();
                    e.printStackTrace();
                }
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







    class getRaceNum extends AsyncTask<Void, String, Void>//다음 경기 유무 확인
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
                URL url = new URL(URL_ADDRESS_GET_RACE_NUMBER);//보낼 주소
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터 전송
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "raceDate=" + data+"&raceNum="+(tempNumber+1);//보낼 정보
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

                // JSONObject 받는 부분
                JSONObject sObject = new JSONObject(page);
                JSONArray sArray = sObject.getJSONArray("dataSend");
                sObject = sArray.getJSONObject(0);
                if (sObject.getString("key").equals("ok")) {
                    LoadData = sObject.getString("key");
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            raceNum.setText(Integer.toString(++tempNumber));
                        }
                    });
                } else {
                   run();
                }
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

        private void setData(String data) {
            this.data = data;
        }

    }


    class StartTimeSender extends AsyncTask<Void, Void, Void> //시작 랩 타임 전송
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
                URL url = new URL(URL_ADDRESS_STARTTIME); // URL클래스의 생성자로 주소를 넘겨준다.
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // 해당 주소의 페이지로 접속을 하고, 단일 HTTP 접속을 하기위해 캐스트한다.
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // 요청 헤더를 정의한다.
                conn.setRequestMethod("POST");//데이터 전송(POST방식)
                conn.setDoOutput(true); // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션을 정의한다.
                conn.setDoInput(true); // IputStream으로 응답 헤더와 메시지를 읽어들이겠다는 옵션을 정의한다.
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "start_time=" + data+"&race_num="+Integer.toString(tempNumber)+"&OnOff=1";//보낼 정보(OnOff, raceNum)
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

        private void setData(String data) {
            this.data = data;
        }
    }


    class FinishTimeSender extends AsyncTask<Void, Void, Void> //종료 랩 타임 전송
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
                URL url = new URL(URL_ADDRESS_FINISHTIME); // URL클래스의 생성자로 주소를 넘겨준다.
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // 해당 주소의 페이지로 접속을 하고, 단일 HTTP 접속을 하기위해 캐스트한다.
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // 요청 헤더를 정의한다.
                conn.setRequestMethod("POST");//데이터 전송(POST방식)
                conn.setDoOutput(true); // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션을 정의한다.
                conn.setDoInput(true); // IputStream으로 응답 헤더와 메시지를 읽어들이겠다는 옵션을 정의한다.
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "stop_time=" + data+"&race_num="+Integer.toString(tempNumber)+"&OnOff=0";//보낼 정보(OnOff, raceNum)
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

        private void setData(String data) {
            this.data = data;
        }
    }


    class SetOnOff extends AsyncTask<Void, Void, Void> //Onoff 조작
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
                URL url = new URL(URL_ADDRESS_SET_ONOFF); // URL클래스의 생성자로 주소를 넘겨준다.
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // 해당 주소의 페이지로 접속을 하고, 단일 HTTP 접속을 하기위해 캐스트한다.
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // 요청 헤더를 정의한다.
                conn.setRequestMethod("POST");//데이터 전송(POST방식)
                conn.setDoOutput(true); // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션을 정의한다.
                conn.setDoInput(true); // IputStream으로 응답 헤더와 메시지를 읽어들이겠다는 옵션을 정의한다.
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "OnOff=" + data+"&raceNum="+Integer.toString(tempNumber);//보낼 정보(OnOff, raceNum)
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

        private void setData(String data) {
            this.data = data;
        }
    }


    class StopTimeSender extends AsyncTask<String, Void, Void> //타이머 멈췄을 때 멈춘 랩타임 시간 형식으로 전송
    {
        private String sendMsg;

        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL(URL_ADDRESS_STOPTIME);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터 전송
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "HOUR=" + strings[0]+"&MINUTE="+strings[1]+"&SECOND="+strings[2]+"&MILISECOND="+strings[3]+"&raceNum="+tempNumber;//보낼 정보
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
