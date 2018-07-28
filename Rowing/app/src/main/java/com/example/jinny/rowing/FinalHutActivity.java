package com.example.jinny.rowing;

import android.Manifest;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class FinalHutActivity extends AppCompatActivity {
    private static final String URL_ADDRESS_TIMER_START = "http://13.209.161.83:8080/start.jsp";
    private static final String URL_ADDRESS_TIME = "http://13.209.161.83:8080/pastTimeSave.jsp";
    final static int IDLE = 0;
    final static int RUNNING = 1;
    File file = Environment.getRootDirectory();
    final String RECORDED_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() + String.format("/recorded.mp4");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    Button bowNumSelectButton[] = new Button[36];
    Button bowNumButton[] = new Button[6];
    LinearLayout dropMenu[] = new LinearLayout[6];
    Integer[] buttonIds1 = {
            R.id.button1_1, R.id.button1_2, R.id.button1_3, R.id.button1_4, R.id.button1_5, R.id.button1_6,
            R.id.button2_1, R.id.button2_2, R.id.button2_3, R.id.button2_4, R.id.button2_5, R.id.button2_6,
            R.id.button3_1, R.id.button3_2, R.id.button3_3, R.id.button3_4, R.id.button3_5, R.id.button3_6,
            R.id.button4_1, R.id.button4_2, R.id.button4_3, R.id.button4_4, R.id.button4_5, R.id.button4_6,
            R.id.button5_1, R.id.button5_2, R.id.button5_3, R.id.button5_4, R.id.button5_5, R.id.button5_6,
            R.id.button6_1, R.id.button6_2, R.id.button6_3, R.id.button6_4, R.id.button6_5, R.id.button6_6};
    Integer[] buttonIds2 = {R.id.bow_number_1, R.id.bow_number_2, R.id.bow_number_3, R.id.bow_number_4, R.id.bow_number_5, R.id.bow_number_6};//bow number 선택하는 버튼(노란거)
    Integer[] menuIds = {R.id.bow_number_list_1, R.id.bow_number_list_2, R.id.bow_number_list_3, R.id.bow_number_list_4, R.id.bow_number_list_5, R.id.bow_number_list_6};
    TextView confirmConnection, raceState, currentDate, currentTime, ongoingTime, firstRecord, secondRecord, thirdRecord, fourthRecord, fifthRecord, sixthRecord, raceNumber, position;
    Button lapButton;
    Button goButton;
    ImageButton stopButton, recordButton, playButton, pauseButton, uploadButton, refreshButton;
    long hBaseTime, hPauseTime;
    int splitCount = 1;
    int lStatus = IDLE;
    int raceNum1;
    String records[] = new String[6];
    String pastTime = null, stringRaceNum = null, stringPosition = null;
    String LoadData, checker = "null";
    private TimerTask mTask;
    private Timer mTimer;

    MediaPlayer player = null;
    MediaRecorder recorder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finalhut);
        currentDate = (TextView) findViewById(R.id.current_date);//날짜
        currentTime = (TextView) findViewById(R.id.current_time);//시간
        ongoingTime = (TextView) findViewById(R.id.hut_ongoing_time);//시작하고나서부터의 시간
        raceNumber = (TextView) findViewById(R.id.hut_race_num);//몇 번 경기
        lapButton = (Button) findViewById(R.id.lap_button);//랩 버튼
        stopButton = (ImageButton) findViewById(R.id.stop_button);//STOP 버튼
        firstRecord = (TextView) findViewById(R.id.first_record);//첫번째 기록
        secondRecord = (TextView) findViewById(R.id.second_record);//두번째 기록
        thirdRecord = (TextView) findViewById(R.id.third_record);//세번째 기록
        fourthRecord = (TextView) findViewById(R.id.fourth_record);//네번째 기록
        fifthRecord = (TextView) findViewById(R.id.fifth_record);//다섯번째 기록
        sixthRecord = (TextView) findViewById(R.id.sixth_record);//여섯번째 기록
        recordButton = (ImageButton) findViewById(R.id.record_button);//녹음 버튼
        playButton = (ImageButton) findViewById(R.id.play_button);//재생 버튼
        pauseButton = (ImageButton) findViewById(R.id.pause_button);//정지 버튼
        uploadButton = (ImageButton) findViewById(R.id.upload_button);//업로드 버튼
        refreshButton = (ImageButton) findViewById(R.id.refresh_button);//초기화 버튼
        confirmConnection = (TextView) findViewById(R.id.hut_confirm_connection);//맨 왼쪽 상단 작은 네모
        position = (TextView) findViewById(R.id.hut_position);//작은 네모 옆에 ~m
        stringPosition = position.getText().toString();//position 문자열
        stringRaceNum = raceNumber.getText().toString();//몇번 경기 문자열
        raceState=(TextView) findViewById(R.id.hut_race_state);//경기중 표시 공간
        raceNum1 = Integer.parseInt(stringRaceNum);//몇번 경기 int 형
        goButton = (Button) findViewById(R.id.hut_go_button);//시작 버튼
        for (int i = 0; i < 6; i++) {
            bowNumButton[i] = (Button) findViewById(buttonIds2[i]);//기록 옆에 노란 버튼
            dropMenu[i] = (LinearLayout) findViewById(menuIds[i]);//노란버튼 누르고 나서 나오는 숫자 리스트
        }
        for (int i = 0; i < 36; i++)
            bowNumSelectButton[i] = (Button) findViewById(buttonIds1[i]);//리스트 안의 숫자들



        pauseButton.setVisibility(View.INVISIBLE);//재생 정지버튼 일단 안보이게

        //녹음버튼
        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //녹음기 버튼 누르면 녹음기 모양 사라지고 정지 버튼 생성
                recordButton.setVisibility(View.INVISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                }
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                recorder.setOutputFile(RECORDED_FILE);//녹음되어 저장될 파일
                try {
                    //녹음 시작
                    Toast.makeText(getApplicationContext(), "녹음을 시작합니다", Toast.LENGTH_LONG).show();
                    recorder.prepare();
                    recorder.start();
                } catch (Exception ex) {
                    Log.e("SampleAudioRecorder", "Exception:", ex);
                }
            }
        });

        //녹음 중지 버튼
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //녹음 정지 버튼
                pauseButton.setVisibility(View.INVISIBLE);
                recordButton.setVisibility(View.VISIBLE);
                if (recorder != null) {
                    try {
                        recorder.stop();
                    } catch (RuntimeException e) {
                        file.delete();  //you must delete the outputfile when the recorder stop failed.
                    } finally {
                        recorder.release();
                        recorder = null;
                    }
                }
                Toast.makeText(getApplicationContext(), "녹음이 중지되었습니다.", Toast.LENGTH_LONG).show();

            }

        });

        //녹음 재생 버튼
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //녹음 파일 재생 버튼
                try {
                    playAudio(RECORDED_FILE);
                    Toast.makeText(getApplicationContext(), "녹음파일 재생 시작됨.", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        //기록 업로드 버튼
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //경기 기록 업로드 버튼
                String hutPosition = position.getText().toString();
                Toast.makeText(getApplicationContext(), "데이터 업로드 중입니다.", Toast.LENGTH_LONG).show();
                try {
                    for (int i = 0; i < 6; i++) {
                        FinalHutActivity.CustomTask a = new FinalHutActivity.CustomTask();
                        String temp = bowNumButton[i].getText().toString();
                        String[] timeTemp = records[i].split(":|[.]");//웹에 넘길 때 "00:00:00.00" 이런 포맷은 특수문자 때문에 넘어가지 않아 시, 분, 초, 밀리초 로 다 나눔
                        a.setData(Integer.parseInt(temp), raceNum1);
                        a.execute(hutPosition, timeTemp[0], timeTemp[1], timeTemp[2], timeTemp[3]);
                    }
                } catch (Exception e) {
                    Toast.makeText(FinalHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                    finish();
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "완료되었습니다.", Toast.LENGTH_LONG).show();
        /*        try {
                    for (int i = 0; i < 6; i++) {
                        TimingHutActivity.Uploader a = new TimingHutActivity.Uploader();
                        String temp = bowNumButton[i].getText().toString();
                        a.execute(stringRaceNum, stringPosition);
                    }
                } catch (Exception e) {
                    Toast.makeText(TimingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                    finish();
                    e.printStackTrace();
                }
                */
            }
        });


        //초기화버튼
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //bow number 버튼들을 초기화 시키고 싶을 때 누르는 새로고침 이미지 버튼
                Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anim);
                refreshButton.startAnimation(anim);
                for (int i = 0; i < bowNumButton.length; i++)
                    bowNumButton[i].setText("0");
                for (int i = 0; i < bowNumSelectButton.length; i++) {
                    bowNumSelectButton[i].setEnabled(true);
                    bowNumSelectButton[i].setBackground(getDrawable(R.drawable.button_border_2));
                }
            }
        });
        showTime();//현재 시간과 날짜 출력하는 함수
        //mHandler.sendEmptyMessage(0);
    }

    /*private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                TimerIsOn b = new TimerIsOn();
                b.execute();
                Log.i(checker,checker+"췌커가 여기서 시작도ㅣㄱㄴ하지? ");
                TimerCaller a = new TimerCaller();
                a.setDate(checker);
                a.execute();
                Log.i(checker,checker+"췌커는 여기란다 ");
                mHandler.sendEmptyMessageDelayed(0,1000);
            } catch (Exception e) {
                Toast.makeText(TimingHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                finish();
                e.printStackTrace();
            }

        }
    };
*/

    private void playAudio(String url) throws Exception {
        //오디오 재생
        killMediaPlayer();
        player = new MediaPlayer();
        player.setDataSource(url);
        player.prepare();
        player.start();
    }

    private void killMediaPlayer() {
        //오디오 끔
        if (player != null) {
            try {
                player.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showTime() {
        final Handler handler = new Handler() {
            //현재 시간과 날짜를 계속 갱신해야하기 때문에 스레드로 구현
            @Override
            public void handleMessage(Message msg) {
                currentDate.setText(dateFormat.format(new Date()));//현재 날짜
                currentTime.setText(timeFormat.format(new Date()));//현재 시간
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
        thread.start();//스레드 시작
    }

    Handler lTimer = new Handler() {
        public void handleMessage(Message msg) {
            ongoingTime.setText(getHutEllapse());
            lTimer.sendEmptyMessage(0);
        }
    };

    protected void onDestroy() {
        lTimer.removeMessages(0);
        super.onDestroy();
    }

    public void hutOnClick(View v) {
        //스톱워치 랩 기능 구현
        switch (v.getId()) {
            case R.id.hut_go_button:
                try {
                    TimerCaller b = new TimerCaller();
                    b.setDate("1");
                    b.execute();
                } catch (Exception e) {
                    Toast.makeText(FinalHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                    finish();
                    e.printStackTrace();
                }
                hBaseTime = SystemClock.elapsedRealtime();
                lTimer.sendEmptyMessage(0);
                lStatus = RUNNING;
                lapButton.setEnabled(true);
                goButton.setVisibility(View.INVISIBLE);
                break;
            case R.id.lap_button:
                String split = String.format("%s", getHutEllapse());
                switch (splitCount) {
                    case 1:
                        firstRecord.setText(split);
                        splitCount++;
                        break;
                    case 2:
                        secondRecord.setText(split);
                        splitCount++;
                        break;
                    case 3:
                        thirdRecord.setText(split);
                        splitCount++;
                        break;
                    case 4:
                        fourthRecord.setText(split);
                        splitCount++;
                        break;
                    case 5:
                        fifthRecord.setText(split);
                        splitCount++;
                        break;
                    case 6:
                        sixthRecord.setText(split);
                        splitCount++;
                        break;
                    default:
                        break;
                }
                break;
            case R.id.stop_button:
                try {
                    TimerCaller b = new TimerCaller();
                    b.setDate("0");
                    b.execute();
                } catch (Exception e) {
                    Toast.makeText(FinalHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                    finish();
                    e.printStackTrace();
                }
                raceState.setBackground(ContextCompat.getDrawable(this, R.drawable.end_state_border));
                raceState.setText(" 경기 종료 ");
                lTimer.removeMessages(0);
                hPauseTime = SystemClock.elapsedRealtime();
                lStatus = IDLE;
                pastTime = ongoingTime.getText().toString();
                String[] timeTemp = pastTime.split(":|[.]");
                try {
                    TimeSender e = new TimeSender();
                    e.execute(timeTemp[0], timeTemp[1], timeTemp[2], timeTemp[3]);
                } catch (Exception e) {
                    Toast.makeText(FinalHutActivity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                    finish();
                    e.printStackTrace();
                }
                break;
        }
        records[0] = firstRecord.getText().toString();
        records[1] = secondRecord.getText().toString();
        records[2] = thirdRecord.getText().toString();
        records[3] = fourthRecord.getText().toString();
        records[4] = fifthRecord.getText().toString();
        records[5] = sixthRecord.getText().toString();
    }

    String getHutEllapse() {
        long now = SystemClock.elapsedRealtime();
        long ell = now - hBaseTime;
        String hEll = String.format("%02d:%02d:%02d.%02d", ell / 1000 / 360, ell / 1000 / 60, (ell / 1000) % 60, (ell % 1000) / 10);
        return hEll;
    }

    //노란버튼 눌렀을 때, 리스트 꺼내는 메소드
    public void menuClick(View view) {
        for (int i = 0; i < dropMenu.length; i++) {
            if (dropMenu[i].getVisibility() == View.VISIBLE) {
                dropMenu[i].setVisibility(View.GONE);
            } else if (view.getId() == buttonIds2[i]) {
                dropMenu[i].setVisibility(View.VISIBLE);
            }
        }
    }

    //리스트의 i 버튼 검은색 & 사용불가
    public void changeButton_black(int i) {
        for (int j = i; j < 36; j = j + 6) {
            bowNumSelectButton[j].setBackground(getDrawable(R.drawable.button_border_3));
            bowNumSelectButton[j].setEnabled(false);
            if (j == i)
                continue;
        }
    }
    //리스트의 i 버튼 노란색 & 사용가능
    public void changeButton_yellow(int i) {
        for (int j = i; j < 36; j = j + 6) {
            bowNumSelectButton[j].setBackground(getDrawable(R.drawable.button_border_2));
            bowNumSelectButton[j].setEnabled(true);
            if (j == i)
                continue;
        }
    }

    public void menuItemClick(View view) {
        int index;
        for (int i = 0; i < buttonIds1.length; i++) {
            if (view.getId() == buttonIds1[i]) {
                index = i / 6;
                int num=Integer.parseInt(bowNumButton[index].getText().toString());
                dropMenu[index].setVisibility(View.GONE);
                bowNumButton[index].setText(bowNumSelectButton[i].getText());
                if (i < 6) {
                    if(num!=0)
                        changeButton_yellow(num-1);
                    changeButton_black(i);
                }
                else if (i >= 6 && i < 12) {
                    if(num!=0)
                        changeButton_yellow(num-1);
                    changeButton_black(i - 6);
                }
                else if (i >= 12 && i < 18) {
                    if(num!=0)
                        changeButton_yellow(num-1);
                    changeButton_black(i - 12);
                }
                else if (i >= 18 && i < 24) {
                    if(num!=0)
                        changeButton_yellow(num-1);
                    changeButton_black(i - 18);
                }
                else if (i >= 24 && i < 30) {
                    if(num!=0)
                        changeButton_yellow(num-1);
                    changeButton_black(i - 24);
                }
                else if (i >= 30 && i < 36) {
                    if(num!=0)
                        changeButton_yellow(num-1);
                    changeButton_black(i - 30);
                }
            }
        }
    }


    class CustomTask extends AsyncTask<String, Void, Void> {
        private int raceNum2, bowNum;
        String sendMsg;

        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL("http://13.209.161.83:8080/record.jsp");//보낼 jsp 주소
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터 전송
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "HUT=" + strings[0] + "&HOUR=" + strings[1] + "&MINUTE=" + strings[2] + "&SECOND=" + strings[3] + "&MILISECOND=" + strings[4] + "&BOWNUM=" + bowNum + "&RACENUM=" + raceNum2;//보낼 정보
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
                Toast.makeText(FinalHutActivity.this, "서버가 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                confirmConnection.setBackground(getDrawable(R.drawable.not_connected));
                super.handleMessage(msg);
            }

        };

        private void setData(int bowNum, int raceNum) {
            this.bowNum = bowNum;
            this.raceNum2 = raceNum;
        }
    }

    class Uploader extends AsyncTask<String, Void, Void> {
        String sendMsg;

        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL("http://13.209.161.83:8080/webRecord.jsp");//보낼 jsp 주소
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터 전송
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "RACE=" + strings[0] + "&HUT=" + strings[1];//보낼 정보
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
                Toast.makeText(FinalHutActivity.this, "서버가 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                confirmConnection.setBackground(getDrawable(R.drawable.not_connected));
                super.handleMessage(msg);
            }

        };
    }

    class TimerCaller extends AsyncTask<Void, Void, Void> {
        private String sendMsg;
        private String data;

        protected Void doInBackground(Void... param) {
            try {
                URL url = new URL(URL_ADDRESS_TIMER_START);
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
                Toast.makeText(FinalHutActivity.this, "서버가 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                confirmConnection.setBackground(getDrawable(R.drawable.not_connected));
                super.handleMessage(msg);
            }
        };

        private void setDate(String date) {
            this.data = date;
        }
    }

    class TimeSender extends AsyncTask<String, Void, Void> {
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
                sendMsg = "HOUR=" + strings[0] + "&MINUTE=" + strings[1] + "&SECOND=" + strings[2] + "&MILISECOND=" + strings[3];//보낼 정보
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
                Toast.makeText(FinalHutActivity.this, "서버가 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                confirmConnection.setBackground(getDrawable(R.drawable.not_connected));
                super.handleMessage(msg);
            }

        };
    }

    class TimerIsOn extends AsyncTask<Void, String, String> {

        protected String doInBackground(Void... param) {
            try {
                URL url = new URL("http://13.209.161.83:8080/hutTimerIsOn.jsp");//보낼 jsp 주소
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터 전송
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
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
                if (sObject.getString("key").equals("on")) {
                    checker = "1";
                } else {
                    checker = "0";
                }

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
            return checker;

        }

        public void noConfirm() {
            confirmHandler.sendEmptyMessage(0);
        }

        private Handler confirmHandler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(FinalHutActivity.this, "서버가 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                confirmConnection.setBackground(getDrawable(R.drawable.not_connected));
                super.handleMessage(msg);
            }

        };
    }
}