package com.ictr.android.cdid;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.IIdentifierListener;
import com.bun.miitmdid.core.MdidSdk;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.supplier.IdSupplier;

/**
 * Created by zheng on 2019/8/22.
 * http://www.msa-alliance.cn/
 */

public class MiitHelper implements IIdentifierListener {

    private OnUpdateOaidListener _listener;

    public MiitHelper(OnUpdateOaidListener callback) {
        _listener = callback;
    }


    public void getDeviceIds(Context cxt) {
        int resultCode = CallFromReflect(cxt);
        switch (resultCode) {
            //不支持的设备
            case ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT:
                //加载配置文件出错
            case ErrorCode.INIT_ERROR_LOAD_CONFIGFILE:
                //不支持的设备厂商
            case ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT:
//                获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程
            case ErrorCode.INIT_ERROR_RESULT_DELAY:
//                反射调用出错
            case ErrorCode.INIT_HELPER_CALL_ERROR:

                break;
            default:
                break;
        }

    }


    /*
     * 通过反射调用，解决android 9以后的类加载升级，导至找不到so中的方法
     *
     * */
    private int CallFromReflect(Context cxt) {
        return MdidSdkHelper.InitSdk(cxt, true, this);
    }

    /*
     * 直接java调用，如果这样调用，在android 9以前没有题，在android 9以后会抛找不到so方法的异常
     * 解决办法是和JLibrary.InitEntry(cxt)，分开调用，比如在A类中调用JLibrary.InitEntry(cxt)，在B类中调用MdidSdk的方法
     * A和B不能存在直接和间接依赖关系，否则也会报错
     *
     * */
    private int DirectCall(Context cxt) {
        MdidSdk sdk = new MdidSdk();
        return sdk.InitSdk(cxt, this);
    }

    @Override
    public void OnSupport(boolean isSupport, IdSupplier _supplier) {
        if (_supplier == null) {
            return;
        }
        String oaid = _supplier.getOAID();
        _supplier.shutDown();
        if (_listener != null) {
            _listener.updateOAID(oaid);
        }
    }

    public interface OnUpdateOaidListener {
        /**
         * 成功获得OAID的回调
         */
        void updateOAID(@NonNull String oaid);
    }


}
