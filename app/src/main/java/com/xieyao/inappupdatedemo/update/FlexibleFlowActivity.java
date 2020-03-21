package com.xieyao.inappupdatedemo.update;

import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.xieyao.inappupdatedemo.BuildConfig;
import com.xieyao.inappupdatedemo.R;

//import com.google.android.material.snackbar.Snackbar;

public class FlexibleFlowActivity extends AppCompatActivity implements InstallStateUpdatedListener {

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
        setContentView(R.layout.activity_flexible_flow);

        // Create instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());

        appUpdateInfoText = findViewById(R.id.app_update_info_text);
        logText = findViewById(R.id.log_text);

        bindUpdateButton();
        bindRefreshAppUpdateInfoButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appUpdateManager != null) {
            appUpdateManager.registerListener(this);
        }

        refreshAppUpdateInfoAndCompleteDownloadedUpdates();
        updateLogText();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (appUpdateManager != null) {
            appUpdateManager.unregisterListener(this);
        }
    }

    @Override
    public void onStateUpdate(InstallState state) {
        log(
                "State: "
                        + toStatusString(state.installStatus())
                        + ", error code: "
                        + state.installErrorCode());

        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == DEV_TRIGGERED_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                log("Update flow failed! Result code: " + resultCode);
            }
        }
    }

    private void prepareUpdateFlow(AppUpdateInfo appUpdateInfo) {
        log("Preparing start update flow...");
        startFlowButton.setVisibility(View.VISIBLE);
        this.appUpdateInfo = appUpdateInfo;
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
                    appUpdateInfo, AppUpdateType.FLEXIBLE, /* activity= */ this, DEV_TRIGGERED_REQUEST_CODE);
        } catch (SendIntentException e) {
            log("Sending pending intent failed:" + e.getMessage());
        }
    }

    private void completeUpdate() {
        log("Update completion requested...");
        appUpdateManager
                .completeUpdate()
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                log("completeUpdate(): successful request");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                log("completeUpdate() failed\n" + e);
                            }
                        });
    }

    /**
     * While refreshing the app update info, we also check whether we have updates in progress to
     * complete.
     *
     * <p>This is important, so the app doesn't forget about downloaded updates even if it gets killed
     * during the download or misses some notifications.
     */
    private void refreshAppUpdateInfoAndCompleteDownloadedUpdates() {
        appUpdateInfoText.setText("Loading app update info...");

        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        new OnSuccessListener<AppUpdateInfo>() {
                            @Override
                            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                                appUpdateInfoText.setText(
                                        String.format(
                                                "Package Name: %s\nCurrent version code: %s\nAvailable version code: %s\n"
                                                        + "Update availability: %s\nCurrent install status: %s",
                                                appUpdateInfo.packageName(),
                                                BuildConfig.VERSION_CODE,
                                                appUpdateInfo.availableVersionCode(),
                                                toUpdateAvailabilityString(appUpdateInfo.updateAvailability()),
                                                appUpdateInfo.installStatus()));

                                // If there is an update available, prepare to promote it.
                                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                                    prepareUpdateFlow(appUpdateInfo);
                                } else {
                                    resetUpdateFlow();
                                }

                                // If already downloaded, start the completion subflow.
                                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                                    popupSnackbarForCompleteUpdate();
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                log("getAppUpdateInfo() failed!\n" + e);
                                appUpdateInfoText.setText(String.format("Failed getting app update info!"));
                            }
                        });
    }

    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.activity_main_layout),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(
                "RELOAD",
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        completeUpdate();
                    }
                });
        snackbar.setActionTextColor(getResources().getColor(R.color.snackbar_action_green));

        snackbar.show();
    }

    private void bindUpdateButton() {
        startFlowButton = findViewById(R.id.update_button);
        startFlowButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearLog();
                        startUpdateFlow();
                    }
                });
        // We don't promote the update until we are sure that we can actually perform one.
        startFlowButton.setVisibility(View.GONE);
    }

    private void bindRefreshAppUpdateInfoButton() {
        refreshAppUpdateInfoButton = findViewById(R.id.refresh_app_update_info_button);
        refreshAppUpdateInfoButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearLog();
                        refreshAppUpdateInfoAndCompleteDownloadedUpdates();
                    }
                });
    }

    private String toStatusString(@InstallStatus int status) {
        switch (status) {
            case 1:
                return "PENDING";
            case 2:
                return "DOWNLOADING";
            case 3:
                return "INSTALLING";
            case 4:
                return "INSTALLED";
            case 5:
                return "FAILED";
            case 6:
                return "CANCELED";
            case 10:
                return "REQUIRES_UI_INTENT";
            case 11:
                return "DOWNLOADED";
            case 0:
            default:
                return "UNKNOWN";
        }
    }

    private String toUpdateAvailabilityString(@UpdateAvailability int updateAvailability) {
        switch (updateAvailability) {
            case 1:
                return "UPDATE_NOT_AVAILABLE";
            case 2:
                return "UPDATE_AVAILABLE";
            case 3:
                return "DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS";
            case 0:
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
