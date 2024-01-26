package com.example.weatherdy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// xml 파일 형식을 data class로 구현
class WEATHER {
    RESPONSE response;
}

class RESPONSE {
    HEADER header;
    BODY body;
}

class HEADER {
    int resultCode;
    String resultMsg;
}

class BODY {
    String dataType;
    ITEMS items;
}

class ITEMS {
    List<ITEM> item;
}

// category : 자료 구분 코드, fcstDate : 예측 날짜, fcstTime : 예측 시간, fcstValue : 예보 값
class ITEM {
    String category;
    String fcstDate;
    String fcstTime;
    String fcstValue;
}

    // retrofit을 사용하기 위한 빌더 생성
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

public class MainActivity extends AppCompatActivity {
    private TextView tvRainRatio;     // 강수 확률
    private TextView tvRainType;      // 강수 형태
    private TextView tvHumidity;      // 습도
    private TextView tvSky;           // 하늘 상태
    private TextView tvTemp;          // 온도
    private Button btnRefresh;        // 새로고침 버튼

    private String base_date = "20210510";  // 발표 일자
    private String base_time = "1400";      // 발표 시각
    private String nx = "0";               // 예보지점 X 좌표
    private String ny = "0";               // 예보지점 Y 좌표

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvRainRatio = findViewById(R.id.tvRainRatio);
        tvRainType = findViewById(R.id.tvRainType);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvSky = findViewById(R.id.tvSky);
        tvTemp = findViewById(R.id.tvTemp);
        btnRefresh = findViewById(R.id.btnRefresh);

        // nx, ny지점의 날씨 가져와서 설정하기
        setWeather(nx, ny);

        // <새로고침> 버튼 누를 때 날씨 정보 다시 가져오기
        btnRefresh.setOnClickListener(v -> setWeather(nx, ny));
    }

    // 날씨 가져와서 설정하기
    private void setWeather(String nx, String ny) {
        // 준비 단계 : base_date(발표 일자), base_time(발표 시각)
        // 현재 날짜, 시간 정보 가져오기
        Calendar cal = Calendar.getInstance();
        base_date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.getTime()); // 현재 날짜
        String time = new SimpleDateFormat("HH", Locale.getDefault()).format(cal.getTime()); // 현재 시간
        // API 가져오기 적당하게 변환
        base_time = getTime(time);
        // 동네예보  API는 3시간마다 현재시간+4시간 뒤의 날씨 예보를 알려주기 때문에
        // 현재 시각이 00시가 넘었다면 어제 예보한 데이터를 가져와야함
        if (base_time.compareTo("2000") >= 0) {
            cal.add(Calendar.DATE, -1);
            base_date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.getTime());
        }

        // 날씨 정보 가져오기
        // (응답 자료 형식-"JSON", 한 페이지 결과 수 = 10, 페이지 번호 = 1, 발표 날짜, 발표 시각, 예보지점 좌표)
        Call<WEATHER> call = ApiObject.retrofitService.GetWeather("JSON", 10, 1, base_date, base_time, nx, ny);

        // 비동기적으로 실행하기
        call.enqueue(new Callback<WEATHER>() {
            // 응답 성공 시
            @Override
            public void onResponse(Call<WEATHER> call, Response<WEATHER> response) {
                if (response.isSuccessful()) {
                    // 날씨 정보 가져오기
                    List<ITEM> it = response.body().response.body.items.item;

                    String rainRatio = "";      // 강수 확률
                    String rainType = "";       // 강수 형태
                    String humidity = "";       // 습도
                    String sky = "";            // 하능 상태
                    String temp = "";           // 기온
                    for (int i = 0; i < 10; i++) {
                        switch (it.get(i).category) {
                            case "POP":
                                rainRatio = it.get(i).fcstValue;
                                break;
                            case "PTY":
                                rainType = it.get(i).fcstValue;
                                break;
                            case "REH":
                                humidity = it.get(i).fcstValue;
                                break;
                            case "SKY":
                                sky = it.get(i).fcstValue;
                                break;
                            case "T3H":
                                temp = it.get(i).fcstValue;
                                break;
                            default:
                                continue;
                        }
                    }
                    // 날씨 정보 텍스트뷰에 보이게 하기
                    setWeather(rainRatio, rainType, humidity, sky, temp);

                    // 토스트 띄우기
                    Toast.makeText(getApplicationContext(), it.get(0).fcstDate + ", " + it.get(0).fcstTime + "의 날씨 정보입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            // 응답 실패 시
            @Override
            public void onFailure(Call<WEATHER> call, Throwable t) {
                Log.d("api fail", t.getMessage());
            }
        });
    }

    // 텍스트 뷰에 날씨 정보 보여주기
    private void setWeather(String rainRatio, String rainType, String humidity, String sky, String temp) {
        // 강수 확률
        tvRainRatio.setText(rainRatio + "%");
        // 강수 형태
        String result = "";
        switch (rainType) {
            case "0":
                result = "없음";
                break;
            case "1":
                result = "비";
                break;
            case "2":
                result = "비/눈";
                break;
            case "3":
                result = "눈";
                break;
            case "4":
                result = "소나기";
                break;
            case "5":
                result = "빗방울";
                break;
            case "6":
                result = "빗방울/눈날림";
                break;
            case "7":
                result = "눈날림";
                break;
            default:
                result = "오류";
        }
        tvRainType.setText(result);
        // 습도
        tvHumidity.setText(humidity + "%");
        // 하늘 상태
        result = "";
        switch (sky) {
            case "1":
                result = "맑음";
                break;
            case "3":
                result = "구름 많음";
                break;
            case "4":
                result = "흐림";
                break;
            default:
                result = "오류";
        }
        tvSky.setText(result);
        // 온도
        tvTemp.setText(temp + "°");
    }

    // 시간 설정하기
    // 동네 예보 API는 3시간마다 현재시각+4시간 뒤의 날씨 예보를 보여줌
    // 따라서 현재 시간대의 날씨를 알기 위해서는 아래와 같은 과정이 필요함. 자세한 내용은 함께 제공된 파일 확인
    private String getTime(String time) {
        String result;
        switch (time) {
            case "00":
            case "01":
            case "02":
                result = "2000";
                break;
            case "03":
            case "04":
            case "05":
                result = "2300";
                break;
            case "06":
            case "07":
            case "08":
                result = "0200";
                break;
            case "09":
            case "10":
            case "11":
                result = "0500";
                break;
            case "12":
            case "13":
            case "14":
                result = "0800";
                break;
            case "15":
            case "16":
            case "17":
                result = "1100";
                break;
            case "18":
            case "19":
            case "20":
                result = "1400";
                break;
            default:
                result = "1700";
        }
        return result;
    }
}
