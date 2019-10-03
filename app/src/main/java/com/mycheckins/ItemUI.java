package com.mycheckins;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Service;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileInputStream;

public class ItemUI extends Fragment {

    private View view;

    // Display the item UI
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.item_ui, container, false);

        if(Globals.selectedItem != null) {
            // Load the item ui in read-only mode
            Item item = Globals.selectedItem;

            String[] locationTokens = item.location.split(",");
            Globals.capturedLocation = new Location("");
            Globals.capturedLocation.setLatitude(Double.parseDouble(locationTokens[0]));
            Globals.capturedLocation.setLongitude(Double.parseDouble(locationTokens[1]));

            EditText titleField = view.findViewById(R.id.title_field);
            EditText placeField = view.findViewById(R.id.place_field);
            EditText detailsField = view.findViewById(R.id.details_field);
            Button setDateButton = view.findViewById(R.id.set_date_button);
            TextView locationText = view.findViewById(R.id.location_field);
            ImageView imageField = view.findViewById(R.id.image_field);

            titleField.setText(item.title);
            placeField.setText(item.place);
            detailsField.setText(item.details);
            setDateButton.setText(item.date);
            locationText.setText(item.location);

            try {
                FileInputStream inFile = getActivity().openFileInput(item.imageFilename);
                imageField.setImageBitmap(BitmapFactory.decodeStream(inFile));
                inFile.close();
            } catch(Exception e) {
                Log.e("Item UI", e.getMessage());
            }

            titleField.setEnabled(false);
            placeField.setEnabled(false);
            detailsField.setEnabled(false);
            setDateButton.setEnabled(false);

            view.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
            view.findViewById(R.id.share_button).setVisibility(View.GONE);
            view.findViewById(R.id.take_picture_button).setVisibility(View.GONE);
        } else {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Service.LOCATION_SERVICE);

            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // Check that the GPS permission is enabled
                String[] requiredPermissions = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION };

                for(String permission : requiredPermissions) {
                    if(ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), requiredPermissions, 1);

                        return view;
                    }
                }

                initializeGPSTracker();
            } else {
                Toast.makeText(getActivity(), "The app has no permission to access the GPS, please enable it in the settings menu.", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    // Track the GPS, when a location is captured then display it
    private void initializeGPSTracker() {
        final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Service.LOCATION_SERVICE);
        final TextView locationText = view.findViewById(R.id.location_field);

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        // Load the item ui in create mode, capture the GPS location of the user
        new AlertDialog.Builder(getActivity())
                .setTitle("Heads up!")
                .setMessage("Make sure you have GPS/location service turned on. Move around until the location is captured.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Globals.capturedLocation = location;
                locationText.setText(location.getLatitude() + "," + location.getLongitude());
                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        });
    }
}
