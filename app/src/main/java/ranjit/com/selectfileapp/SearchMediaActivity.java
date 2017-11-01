package ranjit.com.selectfileapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.onecode.stickyheadergrid.adapter.StickyGridAdapter;
import com.onecode.stickyheadergrid.tonicartos.StickyGridHeadersGridView;
import com.onecode.stickyheadergrid.viewholder.BaseViewHolder;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchMediaActivity extends AppCompatActivity implements AdapterView.OnItemClickListener  {

    private final String TAG = this.getClass().getSimpleName();
    StickyGridHeadersGridView mGalleryGridView;
    private GalleryGridViewAdapter mAdapter;
    Boolean isMultiselectEnable = true;
    List<ImageData> imagesItems = null;
    Parcelable state;
    int ResultOk = -1;
    public Set<ImageData> mSelectedImages = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mGalleryGridView = (StickyGridHeadersGridView) findViewById(R.id.gallery_grid);
        mGalleryGridView.setOnItemClickListener(this);
        mSelectedImages = new HashSet<ImageData>();
        setImagesFromDbToAdapter();
    }

    /**
     * setting files to adapter
     */
    private void setImagesFromDbToAdapter() {

        try {
            imagesItems = new ArrayList<>();
            imagesItems.clear();
            getVdos();
            mAdapter = new GalleryGridViewAdapter(this, imagesItems);
            mGalleryGridView.setAdapter(mAdapter);
            mAdapter.appendBottomAll(imagesItems);
            if (state != null) {
                Log.d(TAG, "trying to restore listview state.." + state);
                mGalleryGridView.onRestoreInstanceState(state);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void showSelected(View v){

        ArrayList<String> moveList = new ArrayList();
        StringBuffer sb = new StringBuffer("Selected "+mSelectedImages.size()+" items: \n");

        for (ImageData img : mSelectedImages) {
            moveList.add(String.valueOf(img.getImageUri()));
            sb.append(" "+ new File(img.getImageUri()).getName()+" \n");
        }
        Toast.makeText(SearchMediaActivity.this, "" +sb, Toast.LENGTH_SHORT).show();
    }
    /**
     * check size of selected images
     */
    public void checkSize() {
        Log.d(TAG, "checkSize " + mSelectedImages.size());
        if (mSelectedImages.size() == 0) {
            isMultiselectEnable = true;

        }

    }

    private void getVdos(){

        Cursor vdocursor = null;
        try {
            String[] columns = {MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.TITLE,
            };
            String selectionVideos = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
            Uri queryUri = MediaStore.Files.getContentUri("external");


            vdocursor = getContentResolver().query(queryUri, columns, selectionVideos, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");

            while (vdocursor.moveToNext()) {
                int dataColumnIndex = vdocursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                File vdo = new File(vdocursor.getString(dataColumnIndex));
                ImageData imageData =new ImageData();
                imageData.setImageUri(vdo.toString());
                imageData.setDate_id(getFormattedTimeAsMMYY(vdo.lastModified()));
                imageData.setModified_date(""+getFormattedTimeAsMMMYY(vdo.lastModified()));
                imagesItems.add(imageData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (vdocursor != null) {
                try {
                    vdocursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static int getFormattedTimeAsMMYY(long timeInMillisecs) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMyy");
        String dateString = sdf.format(new Date(timeInMillisecs));
        return Integer.parseInt(dateString);
    }

    public static String getFormattedTimeAsMMMYY(long timeInMillisecs) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM - dd");
        String dateString = sdf.format(new Date(timeInMillisecs));
        return dateString;
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        try {

            ImageData image = mAdapter.getItem(position);
            final String imagename = image.getImageUri();

            Log.d(TAG, "Image Clicked on " + position + " Id " + image.getImageUri());
            File thisFile = new File(imagename);

            String action = getIntent().getAction();
            if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
                Uri thisUri = Uri.parse(thisFile.toString());
                setResult(ResultOk, new Intent().setData(thisUri));
                finish();
            }else {
                try {
                    Log.d(TAG, "Image Clicked on " + position + " Id " + image.getImageUri());
                    if (isMultiselectEnable) {
                        if (!addImage(image)) {
                            removeImage(image);
                        }
                    }
                    mAdapter.getView(position, view, parent);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * add image preview
     *
     * @param image
     * @return
     */

    public boolean addImage(ImageData image) {
        if (mSelectedImages.add(image)) {
            checkSize();
            return true;
        }
        return false;
    }


    /**
     * remove image preview
     *
     * @param image
     * @return
     */
    public boolean removeImage(ImageData image) {
        if (mSelectedImages.remove(image)) {
            checkSize();
            return true;
        }
        return false;
    }

    public boolean containsImage(ImageData image) {
        return mSelectedImages.contains(image);
    }



    /**
     * Adapter fot grid view
     */
    public class GalleryGridViewAdapter extends StickyGridAdapter<ImageData, GalleryGridViewAdapter.ViewHolderX, GalleryGridViewAdapter.HeaderViewHolderX> {
        List<ImageData> imgData;
        Context context;

        ArrayList<ImageData> arraylist;

        public GalleryGridViewAdapter(Context context, List<ImageData> imgDatas) {
            super(context);
            this.imgData = imgDatas;
            this.context = context;
            arraylist = new ArrayList<ImageData>();
            arraylist.addAll(imgData);
        }


        @Override
        protected int headerLayout() {
            return R.layout.header_layout;
        }

        @Override
        protected HeaderViewHolderX headerViewHolder(View root) {
            return new HeaderViewHolderX(root);
        }

        @Override
        protected void populateHeader(final ImageData data, HeaderViewHolderX headerViewHolder) {
            headerViewHolder.title.setVisibility(View.VISIBLE);
            headerViewHolder.title.setText(data.getModified_date());

        }

        @Override
        public long getHeaderId(int position) {
            try {
                ImageData exampleModel = imgData.get(position);
                return exampleModel.getDate_id();
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }

        @Override
        protected int layout() {
            return R.layout.grid_item_gallery_thumbnail;
        }

        @Override
        protected ViewHolderX viewHolder(View root) {
            return new ViewHolderX(root);
        }

        @Override
        protected void populate(ImageData data, ViewHolderX viewHolder) {


            boolean isSelected =  containsImage(data);
            (viewHolder.thumbnail_image_back).setVisibility(isSelected ?
                    View.VISIBLE : View.GONE);

            (viewHolder.thumbnail_image_back_circle).setVisibility(isSelected ?
                    View.VISIBLE : View.GONE);

            (viewHolder.mThumbnail).setScaleX(isSelected ?
                    0.85f : 1f);
            (viewHolder.mThumbnail).setScaleY(isSelected ?
                    0.85f : 1f);


            if (viewHolder.mImage == null || !viewHolder.mImage.equals(data)) {

                try {
                    final String imagename = data.getImageUri();
                    final File thisFile = new File(imagename);
                    viewHolder.number.setText(thisFile.getName());
                    Glide.with(getApplicationContext())
                            .load(Uri.parse("file://" + data.getImageUri()))
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(viewHolder.mThumbnail);

                    viewHolder.mImage = data;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class ViewHolderX extends BaseViewHolder {

            TextView number;
            ImageView mThumbnail;
            ImageView thumbnail_image_back,  thumbnail_image_back_circle;
            ImageData mImage;
            FrameLayout mView;


            public ViewHolderX(View root) {
                super(root);
                number = (TextView) root.findViewById(R.id.text);
                mThumbnail = (ImageView) root.findViewById(R.id.thumbnail_image);
                thumbnail_image_back_circle = (ImageView) root.findViewById(R.id.thumbnail_image_back_circle);
                mView = (FrameLayout) root.findViewById(R.id.frame_layout);
                thumbnail_image_back = (ImageView) root.findViewById(R.id.thumbnail_image_back);

            }
        }

        public class HeaderViewHolderX extends BaseViewHolder {
            TextView title;
            View root;

            public HeaderViewHolderX(View root) {
                super(root);
                this.root = root;
                title = (TextView) root.findViewById(R.id.txtHeader);
            }
        }
    }
}
