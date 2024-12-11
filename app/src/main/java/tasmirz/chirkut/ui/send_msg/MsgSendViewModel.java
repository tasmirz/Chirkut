package tasmirz.chirkut.ui.send_msg;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MsgSendViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MsgSendViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}