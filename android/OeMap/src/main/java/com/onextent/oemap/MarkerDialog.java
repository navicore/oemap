package com.onextent.oemap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;
import com.onextent.android.util.OeLog;
import com.onextent.oemap.presence.Presence;
import com.onextent.oemap.presence.PresenceException;
import com.onextent.oemap.provider.PresenceHelper;

import org.json.JSONException;
import java.util.List;

public class MarkerDialog extends DialogFragment {

    // ejs todo: rewrite with a search box above the list to filter items out

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String s = null;

        Bundle args = getArguments();
        if (args != null)
            s = args.getString(getString(R.string.bundle_spacename));
        if (s == null) throw new NullPointerException("no mapname");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final OeMapActivity activity = (OeMapActivity) getActivity();

        PresenceHelper presenceHelper = new PresenceHelper(getActivity());

        final String spacename = s;

        try {
            //final List<Presence> presences = presenceHelper.getAllPrecenses();
            final List<Presence> presences = presenceHelper.getAllPrecenses(spacename);
            OeLog.d("ejs size ******************** " + (presences == null ? 0 : presences.size()));
            ListAdapter presencesAdapter = new MarkerLabelAdapter(presences);

            builder.setTitle("Find Map Member")
                    .setAdapter(presencesAdapter, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Presence p = presences.get(which);
                            if (!p.getSpaceName().equals(spacename)) return;
                            OeMapFragment f = activity.getMapFrag();
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
        } catch (JSONException e) {
            OeLog.e(e);
        } catch (PresenceException e) {
            OeLog.e(e);
        }
        return builder.create();
    }

    private class MarkerLabelAdapter extends BaseAdapter {

        private final List<Presence> _presences;

        MarkerLabelAdapter(List<Presence> presences) {
            _presences = presences;
        }

        @Override
        public int getCount() {
            if (_presences == null) return 0;
            return _presences.size();
        }

        @Override
        public Object getItem(int i) {
            if (_presences == null) return null;
            Presence p = _presences.get(i);
            return p.getLabel();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            LayoutInflater inflater = (LayoutInflater) getActivity()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TextView v = (TextView) inflater.inflate(android.R.layout.select_dialog_item, null);

            v.setText(_presences.get(i).getLabel());
            return v;
        }
    }
}

