package system.embedded.pierre.info0064;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.LinkedList;
import java.util.List;

import static system.embedded.pierre.info0064.R.id.parent;

/**
 * Created by pierre on 8/1/17.
 */

public class MyAdapter extends ArrayAdapter<BluetoothDevice> {

    public MyAdapter(Context context, List<BluetoothDevice> values) {
        super(context, -1, values);
    }

    public MyAdapter(Context context){
        super(context,-1);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) super.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.simple_layout, parent, false);
        TextView textView1 = (TextView) rowView.findViewById(R.id.firstLine);
        TextView textView2 = (TextView) rowView.findViewById(R.id.secondLine);
        textView1.setText(super.getItem(position).getName());
        textView2.setText(super.getItem(position).getAddress());
        // Change the icon for Windows and iPhone
        return rowView;
    }
}
