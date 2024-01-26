package com.example.weatherdy;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// 결과 xml 파일에 접근해서 정보 가져오기
public interface WeatherInterface {
    // getVilageFcst : 동네 예보 조회
    @GET("getVilageFcst?serviceKey=ZvYPMHcdP5th36amVoHJGtlGw7hwp%2B8HPSBiZJRe2OzO0t6Bh3iqj5UE15%2Fn5LBpkYYILdb3XQ4ElOFgMWha6A%3D%3D")
    Call<WEATHER> GetWeather(@Query("dataType") String data_type,
                             @Query("numOfRows") int num_of_rows,
                             @Query("pageNo") int page_no,
                             @Query("base_date") String base_date,
                             @Query("base_time") String base_time,
                             @Query("nx") String nx,
                             @Query("ny") String ny);
}
