package chess.com;

import android.view.Gravity;
import android.graphics.Typeface;
import android.view.View;
import android.graphics.Color;
import android.content.Context;
import android.view.Window;
import android.os.Build;
import android.os.Vibrator;
import android.widget.TextView;
import android.os.VibrationEffect;
import android.widget.Toast;
import android.widget.LinearLayout;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import org.json.JSONException;

import org.json.JSONObject;

public class Yu {

	public static void vib(Context context, long duration) {
		Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		if (vibrator != null && vibrator.hasVibrator()) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
				vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
			} else {
				vibrator.vibrate(duration);
			}
		}
	}

	public static void setVisi(Window window) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			window.setStatusBarColor(Color.TRANSPARENT);
			window.setNavigationBarColor(Color.TRANSPARENT);
		}
		try {
			Method method = Window.class.getMethod("setDecorFitsSystemWindows", boolean.class);
			if (method != null) {
				method.invoke(window, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static final String FILE_NAME = "android.json";

	public static void writeFile(Context context, String key, String textData) {
		try {
			JSONObject jsonObject = readJsonObjectFromFile(context);
			jsonObject.put(key, textData);
			writeJsonObjectToFile(context, jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String readFile(Context context, String key) {
		try {
			JSONObject jsonObject = readJsonObjectFromFile(context);
			return jsonObject.optString(key, "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private static JSONObject readJsonObjectFromFile(Context context) throws IOException, JSONException {
		File file = new File(context.getExternalFilesDir(null), FILE_NAME);
		JSONObject jsonObject = new JSONObject();

		if (file.exists()) {
			try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
				StringBuilder stringBuilder = new StringBuilder();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line);
				}
				jsonObject = new JSONObject(stringBuilder.toString());
			}
		}

		return jsonObject;
	}

	private static void writeJsonObjectToFile(Context context, JSONObject jsonObject) throws IOException {
		File file = new File(context.getExternalFilesDir(null), FILE_NAME);
		try (FileWriter fileWriter = new FileWriter(file)) {
			fileWriter.write(jsonObject.toString());
			//fileWriter.append("\n");
			fileWriter.flush();
		}
	}
	
	/////
	
	
	
	 
	public static void showToast(Context context,String message) {
		Toast toast = new Toast(context);
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(20, 10, 20, 10);
		layout.setBackgroundResource(R.drawable._shap); // Use drawable background

		TextView textView = new TextView(context);
		textView.setText(message);
		textView.setTextColor(Color.WHITE);
		textView.setTypeface(Typeface.MONOSPACE);
		textView.setTextSize(14);
		textView.setPadding(20, 10, 20, 10);
		layout.addView(textView);

		toast.setView(layout);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setGravity(Gravity.TOP, 0, 200); // Show at the top
		toast.show();
	}
}