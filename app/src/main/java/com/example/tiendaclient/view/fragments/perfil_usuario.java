package com.example.tiendaclient.view.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tiendaclient.R;
import com.example.tiendaclient.models.recibido.ResponseError;
import com.example.tiendaclient.models.recibido.ResponseLoginUser;
import com.example.tiendaclient.models.recibido.ResponseUserPorID;
import com.example.tiendaclient.service.ApiService;
import com.example.tiendaclient.service.RetrofitCliente;
import com.example.tiendaclient.utils.Global;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A simple {@link Fragment} subclass.
 */
public class perfil_usuario extends Fragment {

    public perfil_usuario() {
        // Required empty public constructor
    }
    View vista;
    TextView PerfilNombresCompletos, PerfilUsuario,PerfilDireccion, PerfilCelular, PerfilCorreo, PerfilRol;
    RoundedImageView PerfilFoto;

    String mensaje="";
   // ResponseUserPorID Usua= new ResponseUserPorID();

    Retrofit retrofit;
    ApiService retrofitApi;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vista= inflater.inflate(R.layout.fragment_perfil_usuario, container, false);
        return  vista;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UI();
        llenaDts();
    }

    public void UI(){
        PerfilNombresCompletos=vista.findViewById(R.id.TVPerfilNombres);
        PerfilUsuario=vista.findViewById(R.id.TVPerfilUser);
        PerfilDireccion=vista.findViewById(R.id.TVPerfilDireccion);
        PerfilCelular=vista.findViewById(R.id.TVPerfilCelular);
        PerfilCorreo=vista.findViewById(R.id.TVPerfilCorreo);
        PerfilRol=vista.findViewById(R.id.TVPerfilRol);
        PerfilFoto=vista.findViewById(R.id.PerfilFoto);
        peticion_usuario("1");
    }

    private void peticion_usuario(String id){
        retrofit = RetrofitCliente.getInstance();
        retrofitApi = retrofit.create(ApiService.class);
        Disposable disposable;
        //JsonObject convertedObject = new Gson().fromJson(jsonConf, JsonObject.class);

        disposable = (Disposable) retrofitApi.ObtenerUsuarioporID(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<ResponseUserPorID>>() {
                    @Override
                    public void onNext(Response<ResponseUserPorID> response) {

                        Log.e("code PU",""+response.code());
                        if (response.code()==200) {
                            Global.UserGlobal=response.body();

                        } else  if (response.code()==500) {
                            mensaje = "Internal Server Error";
                        } else{
                            try {
                                JSONObject jObjError = new JSONObject(response.errorBody().string());
                                Gson gson =new Gson();
                                ResponseError staff = gson.fromJson(jObjError.toString(), ResponseError.class);
                                mensaje=staff.getMensaje();
                                Log.e("normal-->400",mensaje);

                            } catch (Exception e) {
                                Log.e("error conversion json",""+e.getMessage());
                            }

                        }
                    }
                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Log.e("Completado","Se cargo usuario");
                       llenaDts();

                    }
                });
    }

    private  void llenaDts(){
        if(getActivity()==null || isRemoving() || isDetached()){
            Log.e("activity","removido de la actividad ");
            return;
        }
        PerfilNombresCompletos.setText(Global.UserGlobal.getNombres()+ " "+ Global.UserGlobal.getApellidos());
        PerfilUsuario.setText("@"+Global.UserGlobal.getUsuario());
        PerfilDireccion.setText(Global.UserGlobal.getDireccion());
        PerfilCelular.setText(Global.UserGlobal.getCelular());
        PerfilCorreo.setText(Global.UserGlobal.getEmail());
        PerfilRol.setText(Global.UserGlobal.getRol());
    }

}