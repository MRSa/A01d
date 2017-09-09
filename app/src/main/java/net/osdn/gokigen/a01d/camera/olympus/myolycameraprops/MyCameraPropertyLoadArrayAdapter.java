package net.osdn.gokigen.a01d.camera.olympus.myolycameraprops;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

class MyCameraPropertyLoadArrayAdapter extends ArrayAdapter<MyCameraPropertySetItems>
{
    private LayoutInflater inflater = null;
    private final int textViewResourceId;
    private List<MyCameraPropertySetItems> listItems = null;

    MyCameraPropertyLoadArrayAdapter(Context context, int resource, List<MyCameraPropertySetItems> objects)
    {
        super(context, resource, objects);
        textViewResourceId = resource;
        listItems = objects;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     *
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view;
        if(convertView != null)
        {
            view = convertView;
        }
        else
        {
            view = inflater.inflate(textViewResourceId, parent, false);
        }
        MyCameraPropertySetItems item = listItems.get(position);
        try
        {
            TextView idView = (TextView) view.findViewWithTag("id");
            idView.setText(item.getItemId());

            TextView titleView = (TextView)view.findViewWithTag("title");
            titleView.setText(item.getItemName());

            TextView infoView = (TextView) view.findViewWithTag("info");
            infoView.setText(item.getItemInfo());

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (view);
    }
}
