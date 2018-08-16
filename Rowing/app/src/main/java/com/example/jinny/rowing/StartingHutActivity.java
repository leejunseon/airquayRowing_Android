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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class StartingHutActivity extends AppCompatActivity {
    public static final String IP="192.168.254.140";
    private static final String URL_ADDRESS_SET_ONOFF = "http://"+IP+":8080/airquayRowing/main/setOnOff";//Onoff 조작 URL
    private static final String URL_ADDRESS_STOPTIME = "http://"+IP+":8080/airquayRowing/main/pastTimeSave";//멈춘 랩 시간 (종료, 리셋) 전송 URL
    private static final String URL_ADDRESS_STARTTIME="http://"+IP+":8080/airquayRowing/main/startTimeSend";//시작 시간 전송 URL
    private static final String URL_ADDRESS_GET_RACE_NUMBER = "http://"+IP+":8080/airquayRowing/main/getRaceNum";//경기 유무 확인
    private static final String URL_UPDATE_RACEINFO="http://"+IP+":8080/airquayRowing/main/updateRaceinfo";
    updateRaceinfo Update;
    final static int IDLE = 0;
    final static int RUNNING = 1;
    final static int PAUSE = 2;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    TextView raceDate, currentTime, ongoingTime, raceState, raceNumber, confirmConnection;
    Button goButton, twoMinutesButton, data, Reset_Button;
    String  Onoff, race_num, StartTime, hEll;

    int tStatus;
    int tempNumber;//현재 경기번호
    String LoadData, pastTime;
    String[] timeTemp,timeSplit;
    boolean flag,TimerOnoff;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startinghut);
        raceDate = (TextView) findViewById(R.id.current_date);//날짜
        currentTime = (TextView) findViewById(R.id.current_time);//현재 시간
        ongoingTime = (TextView) findViewById(R.id.ongoing_time);//Start누르고부터의 시간
        raceState = (TextView) findViewById(R.id.race_state);//~번 경기 옆에 경기 상황
        raceNumber = (TextView) findViewById(R.id.race_num);//~번
        goButton = (Button) findViewById(R.id.go_button);//Start
        goButton.setEnabled(false);
        Reset_Button=(Button)findViewById(R.id.Reset_Button);//Reset
        Reset_Button.setEnabled(false);
        data = (Button) findViewById(R.id.sendData);//다음 경기

        twoMinutesButton = (Button) findViewById(R.id.two_minute_button);//2분전
        confirmConnection = (TextView) findViewById(R.id.confirm_connection);//왼쪽상단 초록색 네모

        raceState.setVisibility(View.INVISIBLE);

        showTime();
        Timer();

        //진행중인 경기정보 가져오고 UI 세팅
        try {
            Update = new updateRaceinfo();
            Update.setData(dateFormat.format(new Date()));
            Update.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
            finish();
            e.printStackTrace();
        }


        data.setOnClickListener(new View.OnClickListener() //다음경기 버튼 눌렀을 때의 event
        {
            public void onClick(View view) {

                try {
                    getRaceNum RaceNum = new getRaceNum();//다음 경기 조회
                    RaceNum.setData(raceDate.getText().toString());
                    RaceNum.execute();
                } catch (Exception e) {
                    Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                    finish();
                    e.printStackTrace();
                }
            }
        });
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

                        pastTime=ongoingTime.getText().toString();
                        timeTemp=pastTime.split(":|[.]");
                        timeSplit=timeTemp[2].split(".");
                        try {
                            StopTimeSender falseTimeSender = new StopTimeSender();
                            falseTimeSender.execute(timeTemp[0],timeTemp[1],timeTemp[2],timeTemp[3]);
                        } catch (Exception e) {
                            Toast.makeText(StartingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                            finish();
                            e.printStackTrace();
                        }

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
        }
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

    public void Timer() {
        @SuppressLint("HandlerLeak") final Handler Timerhandler = new Handler() {
            //현재 시간과 날짜를 계속 갱신해야하기 때문에 스레드로 구현
            @Override
            public void handleMessage(Message msg) {
                Date now = new Date();
                long nowTime = now.getTime();//현재 시간
                long ell;

                try {
                    if(TimerOnoff) {
                        Calendar calendar = new GregorianCalendar(Locale.KOREA);
                        String dateString = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH) + "T" + StartTime;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
                        Date date = sdf.parse(dateString);
                        long startDate = date.getTime();//경기 시작 시간
                        ell = nowTime - startDate;

                        String totalsec = Long.toString(ell / 1000);
                        String tinysec = Long.toString(ell % 100);
                        if (Long.parseLong(tinysec) < 10) {
                            tinysec += "" + tinysec;
                        }
                        String totalmin = Long.toString(Long.parseLong(totalsec) / 60);
                        String sec = Long.toString(Long.parseLong(totalsec) % 60);
                        if (Long.parseLong(sec) < 10)
                            sec = "0" + sec;
                        String hour = Long.toString(Long.parseLong(totalmin) / 60);
                        if (Long.parseLong(hour) < 10)
                            hour = "0" + hour;
                        String min = Long.toString(Long.parseLong(totalmin) % 60);
                        if (Long.parseLong(min) < 10)
                            min = "0" + min;
                        hEll = hour + ":" + min + ":" + sec + "." + tinysec;

                        ongoingTime.setText(hEll);
                    }
                }catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };
        Runnable task = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                    Timerhandler.sendEmptyMessage(0);
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();//스레드 시작
    }

    public void onBackPressed() {
        Update.cancel(true);
        super.onBackPressed();
    }






    class getRaceNum extends AsyncTask<Void, String, Void>//다음 경기 유무 확인, 있을 경우 경기넘버 +1
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
                            raceNumber.setText(Integer.toString(++tempNumber));
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


    class StartTimeSender extends AsyncTask<Void, Void, Void> //시작 랩 타임 전송, Onoff=1
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


    class updateRaceinfo extends AsyncTask<Void, String, Void>//경기정보 받아옴
    {
        private String data;
        String sendMsg;

       /* protected void onPreExecute() {
            pDialog = new ProgressDialog(StartingHutActivity.this);
            pDialog.setMessage("검색중입니다...");
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();
        }*/

        protected Void doInBackground(Void... param) {
            while (!isCancelled()) {
                try {
                    URL url = new URL(URL_UPDATE_RACEINFO);//보낼 주소
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestMethod("POST");//데이터 전송
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setDefaultUseCaches(false);
                    OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                    sendMsg = "raceDate=" + data+"&Hut=Start";//보낼 정보
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

                    //경기번호
                    sObject = sArray.getJSONObject(0);
                    race_num = sObject.getString("race_num");
                    tempNumber=Integer.parseInt(race_num);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            raceNumber.setText(race_num);
                        }
                    });

                    //경기 상태
                    sObject = sArray.getJSONObject(1);
                    Onoff = sObject.getString("Onoff");
                    publishProgress(Onoff);

                    //시작시간
                    sObject = sArray.getJSONObject(2);
                    StartTime = sObject.getString("StartTime");

                    conn.disconnect();
                    Thread.sleep(1000);

                }catch (MalformedURLException | ProtocolException exception) {
                    if(!isCancelled())
                        noConfirm();
                    exception.printStackTrace();
                    finish();
                } catch (IOException io) {
                    if(!isCancelled())
                        //noConfirm();
                    io.printStackTrace();
                } catch (JSONException e) {
                    if(!isCancelled())
                        noConfirm();
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    if(!isCancelled())
                        noConfirm();
                    e.printStackTrace();
                }
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

        protected void onProgressUpdate(String... progress){
            if(progress[0].equals("0")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        raceState.setBackground(getDrawable(R.drawable.end_state_border));
                        raceState.setText(" 경기 종료 ");
                        raceState.setVisibility(View.VISIBLE);
                        tStatus=IDLE;
                        TimerOnoff=false;
                        twoMinutesButton.setEnabled(true);
                        goButton.setText(" Start ");
                        goButton.setEnabled(false);
                    }
                });
            }
            else if(progress[0].equals("2")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        goButton.setEnabled(true);
                        raceState.setBackground(getDrawable(R.drawable.two_minutes_state_border));
                        raceState.setText(" 2 분전 ");
                        raceState.setVisibility(View.VISIBLE);
                    }
                });
            }
            else if(progress[0].equals("1")){

                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TimerOnoff=true;
                            tStatus = RUNNING;
                            raceState.setVisibility(View.VISIBLE);
                            raceState.setText(" 경기중 ");
                            raceState.setBackground(getDrawable(R.drawable.ongoing_state_border));
                            twoMinutesButton.setEnabled(false);
                            goButton.setText(" False ");
                            Reset_Button.setEnabled(false);
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            else if(progress[0].equals("3")){
                raceState.setBackground(getDrawable( R.drawable.end_state_border));
                raceState.setText(" 대기 ");
                Reset_Button.setEnabled(true);
                goButton.setEnabled(false);
                goButton.setText(" Start ");
                tStatus = PAUSE;
            }
            else if(progress[0].equals("4")) {
                ongoingTime.setText(getReset());
                goButton.setEnabled(true);
            }
            else{
                run();
            }
        }

        private void setData(String data) {
            this.data = data;
        }

    }

}


