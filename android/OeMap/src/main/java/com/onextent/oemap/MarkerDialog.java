package com.onextent.oemap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.presence.Presence;
import com.onextent.oemap.presence.PresenceException;
import com.onextent.oemap.provider.PresenceHelper;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MarkerDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String space = null;
        Bundle args = getArguments();
        if (args != null)
            space = args.getString(getString(R.string.bundle_spacename));
        if (space == null) throw new NullPointerException("no mapname");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] array = null; //ejs todo: clean up, this is insane
        final OeMapActivity activity = (OeMapActivity) getActivity();
        PresenceHelper prefHelper = new PresenceHelper(getActivity());
        final List<Presence> presList = new ArrayList<Presence>();
        try {
            Set<Presence> presences = prefHelper.getAllPrecenses(space);
            if (presences != null)
                for (Presence p : presences) {
                    presList.add(p);
                }
            OeLog.d("ejs found " + presList.size() + " presences ******************");
            array = new String[presList.size()];
            for (int i = 0; i < presList.size(); i++) {
                array[i] = presList.get(i).getLabel();
            }
        } catch (JSONException e) {
            OeLog.e(e);
        } catch (PresenceException e) {
            OeLog.e(e);
        }
        ArrayAdapter<String> a = new ArrayAdapter<String>(getActivity(), android.R.layout.select_dialog_item, array);

        builder.setTitle("Select a Cohort")
           .setAdapter(a, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {

                   Presence p = presList.get(which);
                   OeMapFragment f =  activity.getMapFrag();
                   if (f != null) {
                       OeMapFragment.Holder h = f.getMarkers().remove(p.getUID());
                       if (h != null) { //move selected marker to the top by adding it last
                           h.marker.remove();
                           Marker m = f.updateMarker(p);
                           m.showInfoWindow();
                           f.setLocation(p);
                       }
                   }
           }
    });
    return builder.create();
    }
}

