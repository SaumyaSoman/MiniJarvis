package edu.winlab.minijarvis.activity;

import java.util.ArrayList;

import edu.winlab.minijarvis.R;
import edu.winlab.minijarvis.model.SearchResults;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ResponseListBaseAdapter extends BaseAdapter {
	
	private static ArrayList<SearchResults> resultsList;
	private LayoutInflater l_Inflater;

	public ResponseListBaseAdapter(Context context, ArrayList<SearchResults> results) {
		resultsList = results;
		System.out.println("hehe"+resultsList.size());
		l_Inflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return resultsList.size();
	}

	public Object getItem(int position) {
		return resultsList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = l_Inflater.inflate(R.layout.search_results, null);
			holder = new ViewHolder();
			holder.txt_title = (TextView) convertView.findViewById(R.id.title);
			holder.txt_link = (TextView) convertView.findViewById(R.id.link);
			holder.txt_snippet = (TextView) convertView.findViewById(R.id.snippet);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.txt_title.setText(resultsList.get(position).getTitle());
		holder.txt_link.setText(resultsList.get(position).getLink());
		holder.txt_snippet.setText(resultsList.get(position).getSnippet());
		
		return convertView;
	}

	static class ViewHolder {
		TextView txt_title;
		TextView txt_link;
		TextView txt_snippet;
	}
}
