package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.ArrayList;
import java.util.Locale;

    public class TelemetryRecorder {

        public static class TelemetryPoint {
            public float t;   // seconds since run start
            public float x;   // world x
            public float y;   // world y

            public TelemetryPoint() {}
            public TelemetryPoint(float t, float x, float y) {
                this.t = t; this.x = x; this.y = y;
            }
        }

        public static class TelemetrySession {
            public int level;
            public String result;     // "win" / "lose" / "quit"
            public long startedAtMs;  // System.currentTimeMillis()
            public float durationSec;

            public ArrayList<TelemetryPoint> points = new ArrayList<>();
        }

        private final TelemetrySession session = new TelemetrySession();

        // Recording controls (prevents huge files)
        private float elapsedSec = 0f;
        private float recordAccumulator = 0f;

        // Change these if you want
        private float recordIntervalSec = 0.10f;  // record every 0.10s (10 Hz)
        private float minMoveDistance = 2.0f;     // only record if moved >= 2 units

        private Float lastX = null;
        private Float lastY = null;

        public void startSession(int level) {
            session.level = level;
            session.result = "unknown";
            session.startedAtMs = System.currentTimeMillis();
            session.durationSec = 0f;
            session.points.clear();

            elapsedSec = 0f;
            recordAccumulator = 0f;
            lastX = null;
            lastY = null;
        }

        /** Call every frame (or on a timer) */
        public void update(float deltaSec, float playerX, float playerY) {
            elapsedSec += deltaSec;
            recordAccumulator += deltaSec;

            if (recordAccumulator < recordIntervalSec) return;
            recordAccumulator = 0f;

            if (lastX == null || lastY == null) {
                addPoint(playerX, playerY);
                return;
            }

            float dx = playerX - lastX;
            float dy = playerY - lastY;
            float dist2 = dx * dx + dy * dy;

            if (dist2 >= minMoveDistance * minMoveDistance) {
                addPoint(playerX, playerY);
            }
        }

        private void addPoint(float x, float y) {
            session.points.add(new TelemetryPoint(elapsedSec, x, y));
            lastX = x;
            lastY = y;
        }

        public void endSession(String result) {
            session.result = result;
            session.durationSec = elapsedSec;
        }

        /** Exports JSON to local storage and returns absolute path */
        public String exportJson() {
            FileHandle dir = Gdx.files.local("telemetry");
            dir.mkdirs();

            String fileName = "run_" + session.startedAtMs + "_L" + session.level + "_" + session.result + ".json";
            FileHandle out = dir.child(fileName);

            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            json.setUsePrototypes(false);

            out.writeString(json.prettyPrint(session), false, "UTF-8");
            return out.file().getAbsolutePath();
        }

        /** Exports CSV to local storage and returns absolute path */
        public String exportCsv() {
            FileHandle dir = Gdx.files.local("telemetry");
            dir.mkdirs();

            String fileName = "run_" + session.startedAtMs + "_L" + session.level + "_" + session.result + ".csv";
            FileHandle out = dir.child(fileName);

            StringBuilder sb = new StringBuilder();
            sb.append("t,x,y\n");
            for (TelemetryPoint p : session.points) {
                sb.append(String.format(Locale.US, "%.3f,%.3f,%.3f\n", p.t, p.x, p.y));
            }

            out.writeString(sb.toString(), false, "UTF-8");
            return out.file().getAbsolutePath();
        }

        public TelemetrySession getSession() {
            return session;
        }
    }
