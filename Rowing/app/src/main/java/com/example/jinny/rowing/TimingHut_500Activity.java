package com.example.jinny.rowing;

import android.Manifest;
import android.annotation.SuppressLint;
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
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import static com.example.jinny.rowing.StartingHutActivity.IP;

public class TimingHut_500Activity extends AppCompatActivity {
    private static final String URL_RECORD = "http://"+IP+":8080/airquayRowing/main/recordUpload";
    private static final String URL_UPDATE_RACEINFO="http://"+IP+":8080/airquayRowing/main/updateRaceinfo";
    private static final String URL_NEXTRACENUM="http://"+IP+":8080/airquayRowing/main/nextRacenum";
    updateRaceinfo Update;
    private ProgressDialog pDialog;
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
    ImageButton nextraceButton, recordButton, playButton, pauseButton, uploadButton, refreshButton;
    int splitCount = 1;
    String records[] = new String[6];
    String stringRaceNum = null, stringPosition = null;
    String  Onoff, race_num,day_race_num, StartTime,FinishTime;
    String hEll;
    int tempNumber;
    boolean TimerOnoff;

    MediaPlayer player = null;
    MediaRecorder recorder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timinghut_500);
        currentDate = (TextView) findViewById(R.id.current_date);//날짜
        currentTime = (TextView) findViewById(R.id.current_time);//시간
        ongoingTime = (TextView) findViewById(R.id.hut_ongoing_time);//시작하고나서부터의 시간
        raceNumber = (TextView) findViewById(R.id.hut_race_num);//몇 번 경기
        lapButton = (Button) findViewById(R.id.lap_button);//랩 버튼
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
        nextraceButton=(ImageButton)findViewById(R.id.next_button);
        refreshButton = (ImageButton) findViewById(R.id.refresh_button);//초기화 버튼
        confirmConnection = (TextView) findViewById(R.id.hut_confirm_connection);//맨 왼쪽 상단 작은 네모
        position = (TextView) findViewById(R.id.hut_position);//작은 네모 옆에 ~m
        stringPosition = position.getText().toString();//position 문자열
        raceState=(TextView) findViewById(R.id.hut_race_state);//경기중 표시 공간
        for (int i = 0; i < 6; i++) {
            bowNumButton[i] = (Button) findViewById(buttonIds2[i]);//기록 옆에 노란 버튼
            dropMenu[i] = (LinearLayout) findViewById(menuIds[i]);//노란버튼 누르고 나서 나오는 숫자 리스트
        }
        for (int i = 0; i < 36; i++)
            bowNumSelectButton[i] = (Button) findViewById(buttonIds1[i]);//리스트 안의 숫자들

        showTime();//현재 시간과 날짜 출력하는 함수
        Timer();

        //진행중인 경기정보 가져오고 UI 세팅
        try {
            Update = new updateRaceinfo();
            Update.setData(dateFormat.format(new Date()));
            Update.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Toast.makeText(TimingHut_500Activity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
            finish();
            e.printStackTrace();
        }


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
                        CustomTask a = new CustomTask();
                        int temp = Integer.parseInt(bowNumButton[i].getText().toString());//temp = bow_num
                        String[] timeTemp = records[i].split(":|[.]");//웹에 넘길 때 "00:00:00.00" 이런 포맷은 특수문자 때문에 넘어가지 않아 시, 분, 초, 밀리초 로 다 나눔
                        a.setData(temp, Integer.parseInt(race_num),i+1);
                        a.execute(hutPosition, timeTemp[0], timeTemp[1], timeTemp[2], timeTemp[3]);
                    }
                }catch(NullPointerException e){
                    Toast.makeText(TimingHut_500Activity.this, "기록이 입력되지 않았습니다.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }catch (Exception e) {
                    Toast.makeText(TimingHut_500Activity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                    finish();
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), "완료되었습니다.", Toast.LENGTH_LONG).show();
            }
        });

        //다음 경기
        nextraceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try {
                    NextRace next=new NextRace();
                    next.setData(currentDate.getText().toString());
                    next.execute();
                } catch (Exception e) {
                    Toast.makeText(TimingHut_500Activity.this, "서버와 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                    finish();
                    e.printStackTrace();
                }
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

        lapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //스톱워치 랩 기능 구현
                String split = ongoingTime.getText().toString();
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
                records[0] = firstRecord.getText().toString();
                records[1] = secondRecord.getText().toString();
                records[2] = thirdRecord.getText().toString();
                records[3] = fourthRecord.getText().toString();
                records[4] = fifthRecord.getText().toString();
                records[5] = sixthRecord.getText().toString();
            }
        } );
    }

    String getReset()
    {
        String sEll=String.format("%02d:%02d:%02d.%02d",00,00,00,00) ;
        return sEll;
    }

    public void onBackPressed() {
        Update.cancel(true);
        super.onBackPressed();
    }

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
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    handler.sendEmptyMessage(2);
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();//스레드 시작
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
        private int raceNum2, bowNum, rank;
        String sendMsg;

        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL(URL_RECORD);//보낼 jsp 주소
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터 전송
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "HUT=" + strings[0] + "&HOUR=" + strings[1] + "&MINUTE=" + strings[2] + "&SECOND=" + strings[3] + "&MILISECOND=" + strings[4] + "&BOWNUM=" + bowNum + "&RACENUM=" + raceNum2 + "&RANK="+rank;//보낼 정보
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
                Toast.makeText(TimingHut_500Activity.this, "서버가 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                confirmConnection.setBackground(getDrawable(R.drawable.not_connected));
                super.handleMessage(msg);
            }

        };

        private void setData(int bowNum, int raceNum,int rank) {
            this.bowNum = bowNum;
            this.raceNum2 = raceNum;
            this.rank=rank;
        }
    }


    class NextRace extends AsyncTask<Void, String, Void>//다음 경기 유무 확인, 있을 경우 경기넘버 +1
    {
        private String data;
        String sendMsg;

        protected void onPreExecute() {
            pDialog = new ProgressDialog(TimingHut_500Activity.this);
            pDialog.setMessage("검색중입니다...");
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();
        }

        protected Void doInBackground(Void... param) {
            try {
                URL url = new URL(URL_NEXTRACENUM);//보낼 주소
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");//데이터 전송
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setDefaultUseCaches(false);
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "raceNum="+Integer.toString(tempNumber)+"&Hut="+position.getText().toString();//보낼 정보
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
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            raceNumber.setText(Integer.toString(Integer.parseInt(day_race_num)+1));
                            firstRecord.setText("00:00:00.00");
                            secondRecord.setText("00:00:00.00");
                            thirdRecord.setText("00:00:00.00");
                            fourthRecord.setText("00:00:00.00");
                            fifthRecord.setText("00:00:00.00");
                            sixthRecord.setText("00:00:00.00");
                            for (int i = 0; i < bowNumButton.length; i++)
                                bowNumButton[i].setText("0");
                            for (int i = 0; i < bowNumSelectButton.length; i++) {
                                bowNumSelectButton[i].setEnabled(true);
                                bowNumSelectButton[i].setBackground(getDrawable(R.drawable.button_border_2));
                            }
                            splitCount=1;
                        }
                    });
                } else {
                    run();
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
            return null;

        }

        public void noConfirm() {
            confirmHandler.sendEmptyMessage(0);
        }

        private Handler confirmHandler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(TimingHut_500Activity.this, "서버가 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                confirmConnection.setBackground(getDrawable(R.drawable.not_connected));
                super.handleMessage(msg);
            }

        };

        private void run() {
            handler.sendEmptyMessage(0);
        }

        private Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(TimingHut_500Activity.this, "오늘 경기는 끝났습니다.", Toast.LENGTH_LONG).show();
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

    class updateRaceinfo extends AsyncTask<Void, String, Void>//경기정보 받아옴
    {
        private String data;
        String sendMsg;

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
                    sendMsg = "raceDate=" + data+"&Hut="+position.getText().toString();//보낼 정보
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

                    //경기 상태
                    sObject = sArray.getJSONObject(1);
                    Onoff = sObject.getString("Onoff");
                    publishProgress(Onoff);

                    //시작시간
                    sObject = sArray.getJSONObject(2);
                    StartTime = sObject.getString("StartTime");

                    //종료시간
                    sObject=sArray.getJSONObject(3);
                    FinishTime=sObject.getString("FinishTime");

                    //해당 날짜 레이스 번호
                    sObject=sArray.getJSONObject(4);
                    day_race_num=sObject.getString("day_race_num");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            raceNumber.setText(day_race_num);
                        }
                    });

                    conn.disconnect();
                    Thread.sleep(1000);

                } catch (MalformedURLException | ProtocolException exception) {
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
                Toast.makeText(TimingHut_500Activity.this, "서버가 연결이 되지 않습니다.", Toast.LENGTH_LONG).show();
                confirmConnection.setBackground(getDrawable(R.drawable.not_connected));
                super.handleMessage(msg);
            }

        };

        private void run() {
            handler.sendEmptyMessage(0);
        }

        private Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(TimingHut_500Activity.this, "오늘 경기는 끝났습니다.", Toast.LENGTH_LONG).show();
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
                        raceState.setVisibility(View.VISIBLE);
                        TimerOnoff=false;
                        ongoingTime.setText(FinishTime);
                        raceState.setBackground(getDrawable(R.drawable.end_state_border));
                        raceState.setText(" 경기 종료 ");
                    }
                });
            }
            else if(progress[0].equals("2")){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        raceState.setVisibility(View.VISIBLE);
                        raceState.setBackground(getDrawable(R.drawable.two_minutes_state_border));
                        raceState.setText(" 2 분전 ");                    }
                });
            }
            else if(progress[0].equals("1")){

                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TimerOnoff=true;
                            raceState.setText(" 경기중 ");
                            raceState.setBackground(getDrawable(R.drawable.ongoing_state_border));
                        }
                    });
                }catch(Exception e){
                    e.printStackTrace();
                }
            }else if(progress[0].equals("3")){
                raceState.setBackground(getDrawable( R.drawable.end_state_border));
                raceState.setText(" 대기 ");
                TimerOnoff=false;
            }
            else if(progress[0].equals("4")) {
                ongoingTime.setText(getReset());
            }
            else if(progress[0].equals("5")) {
                raceState.setVisibility(View.INVISIBLE);
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
