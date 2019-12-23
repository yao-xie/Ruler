package com.xieyao.inappupdatedemo;

import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

public class ImmediateFlowActivity extends AppCompatActivity {

    private static final int DEV_TRIGGERED_REQUEST_CODE = 1;

    private AppUpdateManager appUpdateManager;

    private Button startFlowButton;
    private Button refreshAppUpdateInfoButton;

    private TextView appUpdateInfoText;
    private TextView logText;

    private AppUpdateInfo appUpdateInfo = null;

    private StringBuilder log = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_immediate_flow);

        // Create instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(this);

        appUpdateInfoText = findViewById(R.id.app_update_info_text);
        logText = findViewById(R.id.log_text);

        bindUpdateButton();
        bindRefreshAppUpdateInfoButton();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshAppUpdateInfoAndResumeFlowUiIfUpdateInProgress();
        updateLogText();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEV_TRIGGERED_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                log("Update flow failed! Result code: " + resultCode);
            }
        }
    }

    private void prepareUpdateFlow(AppUpdateInfo appUpdateInfo) {
        log("Preparing start update flow...");
        this.appUpdateInfo = appUpdateInfo;
        startFlowButton.setVisibility(View.VISIBLE);
    }

    private void resumeFlowUi(AppUpdateInfo appUpdateInfo) {
        this.appUpdateInfo = appUpdateInfo;
        clearLog();
        startUpdateFlow();
    }

    private void resetUpdateFlow() {
        startFlowButton.setVisibility(View.GONE);
        appUpdateInfo = null;
    }

    private void startUpdateFlow() {
        if (appUpdateInfo == null) {
            log("Missing intent to start the flow!");
        }

        try {
            log("Starting update flow...");
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo, AppUpdateType.IMMEDIATE, /* activity= */ this, DEV_TRIGGERED_REQUEST_CODE);
        } catch (SendIntentException e) {
            log("Sending pending intent failed:" + e.getMessage());
        }
    }

    /**
     * While refreshing the app update info, we also check whether we have updates in progress to
     * complete.
     *
     * <p>This is important, so we can resume the flow UI again if an update is already happening.
     * Note that the download has proceeded in the background; we just need to hook the UI back into
     * the In-App Update flow, so the user can follow the download progress and is forced not to use
     * the app while the update happens.
     *
     * <p>In case an update flow is in progress, the update can be resumed in the same way it could be
     * originally started.
     */
    private void refreshAppUpdateInfoAndResumeFlowUiIfUpdateInProgress() {
        appUpdateInfoText.setText("Loading app update info...");

        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            appUpdateInfoText.setText(
                                    String.format(
                                            "Package Name: %s\nCurrent version code: %s\nAvailable version code: %s\n"
                                                    + "Update availability: %s\nCurrent install status: %s",
                                            appUpdateInfo.packageName(),
                                            BuildConfig.VERSION_CODE,
                                            appUpdateInfo.availableVersionCode(),
                                            toUpdateAvailabilityString(appUpdateInfo.updateAvailability()),
                                            appUpdateInfo.installStatus()));

                            if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                                    // If there is an update available, prepare to promote it.
                                    prepareUpdateFlow(appUpdateInfo);
                                } else if (appUpdateInfo.updateAvailability()
                                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                    // If an in-app update is already running, launch the flow UI.
                                    resumeFlowUi(appUpdateInfo);
                                }
                            } else {
                                resetUpdateFlow();
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            log("getAppUpdateInfo() failed!\n" + e);
                            appUpdateInfoText.setText(String.format("Failed getting app update info!"));
                        });
    }

    private void bindUpdateButton() {
        startFlowButton = findViewById(R.id.update_button);
        startFlowButton.setOnClickListener(
                v -> {
                    clearLog();
                    startUpdateFlow();
                });
        // We don't promote the update until we are sure that we can actually perform one.
        startFlowButton.setVisibility(View.GONE);
    }

    private void bindRefreshAppUpdateInfoButton() {
        refreshAppUpdateInfoButton = findViewById(R.id.refresh_app_update_info_button);
        refreshAppUpdateInfoButton.setOnClickListener(
                v -> {
                    clearLog();
                    refreshAppUpdateInfoAndResumeFlowUiIfUpdateInProgress();
                });
    }

    private String toUpdateAvailabilityString(@UpdateAvailability int updateAvailability) {
        switch (updateAvailability) {
            case UpdateAvailability.UPDATE_NOT_AVAILABLE:
                return "UPDATE_NOT_AVAILABLE";
            case UpdateAvailability.UPDATE_AVAILABLE:
                return "UPDATE_AVAILABLE";
            case UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS:
                return "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS";
            default:
                return "UNKNOWN";
        }
    }

    private void clearLog() {
        log.setLength(0);
        updateLogText();
    }

    private void log(String message) {
        log.append("\n").append(message);
        updateLogText();
    }

    private void updateLogText() {
        logText.setText(log.toString());
    }
}