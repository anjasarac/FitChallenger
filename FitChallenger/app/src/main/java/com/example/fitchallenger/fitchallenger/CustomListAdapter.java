package com.example.fitchallenger.fitchallenger;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> itemname;
    private final ArrayList<Integer> imgid;
    private final ArrayList<String> imgUser;
    private final ArrayList<String>  points;

    public CustomListAdapter(Activity context, ArrayList<String> itemname, ArrayList<Integer> imgid,ArrayList<String> imgUser,ArrayList<String> points) {
        super(context, R.layout.mylist, itemname);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemname=itemname;
        this.imgid=imgid;
        this.points = points;
        this.imgUser = imgUser;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.mylist, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView extratxt = (TextView) rowView.findViewById(R.id.textView1);

        txtTitle.setText(itemname.get(position));

      if(imgUser == null)
      {
          imageView.setImageResource(imgid.get(position));
          extratxt.setText(points.get(position));
      }
      else
      {
          Picasso.with(context)
                  .load(imgUser.get(position))
                  .into(imageView);
          extratxt.setText(points.get(position));
      }



        return rowView;

    };
}
