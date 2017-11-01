package ranjit.com.selectfileapp;

import android.os.Parcel;
import android.os.Parcelable;


public class ImageData implements Parcelable {


    private String imageUri = "";
    private String modified_date = "";
    private int date_id = 0;


    public String getModified_date() {
        //MMM-YYYY
        return modified_date;
    }

    public void setModified_date(String modified_date) {
        ///MMM-yyyy
        this.modified_date = modified_date;
    }

    public int getDate_id() {
        return date_id;
    }

    public void setDate_id(int date_id) {
        //102016
        this.date_id = date_id;
    }


    public ImageData() {
    }


    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(imageUri);
        dest.writeString(modified_date);
        dest.writeInt(date_id);
    }

    public static final Creator<ImageData> CREATOR = new Creator<ImageData>() {
        @Override
        public ImageData createFromParcel(Parcel in) {
            return new ImageData(in);
        }

        @Override
        public ImageData[] newArray(int size) {
            return new ImageData[size];
        }
    };

    protected ImageData(Parcel in) {
        imageUri = in.readString();
        modified_date = in.readString();
        date_id = in.readInt();
    }


}
