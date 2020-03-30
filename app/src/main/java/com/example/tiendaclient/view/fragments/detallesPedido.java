package com.example.tiendaclient.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tiendaclient.R;
import com.example.tiendaclient.adapter.VistasDetallePedido;
import com.example.tiendaclient.models.recibido.DetallesP;
import com.example.tiendaclient.models.recibido.ResponseDetallesPedidos;
import com.example.tiendaclient.models.recibido.ResponseError;
import com.example.tiendaclient.models.recibido.ResponseVerPedido;
import com.example.tiendaclient.service.ApiService;
import com.example.tiendaclient.service.RetrofitCliente;
import com.example.tiendaclient.utils.Global;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.Retrofit;


public class detallesPedido extends Fragment {

    List<DetallesP> LstPro = new ArrayList<>();
    ResponseDetallesPedidos pedido = new ResponseDetallesPedidos();
    Retrofit retrofit;
    public String id_pedido;
    ApiService retrofitApi;
    Boolean continuar=false;
    String mensaje="";
    RecyclerView recyclerView;
    VistasDetallePedido adapter;
    TextView NumeroPedido,NombreTrasnportista,PedidoTxtStatus,PedidoCelular,DetalleSubtotal,DetalleCostoEnvio,DetalleTotal,PedidoFecha;
        LinearLayout PedidoStatus;
        RoundedImageView atras_detalle_pedido;
        View vista;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vista= inflater.inflate(R.layout.fragment_detalles_pedido, container, false);
        return vista;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        UI();
        peticion_pedidos();
    }

    private void UI(){

        PedidoFecha=vista.findViewById(R.id.PedidoFecha);

        NumeroPedido=vista.findViewById(R.id.NumeroPedido);
        NombreTrasnportista=vista.findViewById(R.id.NombreTrasnportista);
        PedidoTxtStatus=vista.findViewById(R.id.PedidoTxtStatus);
        PedidoCelular=vista.findViewById(R.id.PedidoCelular);
        DetalleSubtotal=vista.findViewById(R.id.DetalleSubtotal);
        DetalleCostoEnvio=vista.findViewById(R.id.DetalleCostoEnvio);
        DetalleTotal=vista.findViewById(R.id.DetalleTotal);
        PedidoStatus=vista.findViewById(R.id.PedidoStatus);
        atras_detalle_pedido=vista.findViewById(R.id.atras_detalle_pedido);
        atras_detalle_pedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
    }


    private void llenarDatos(){

        NumeroPedido.setText("Pedido # "+pedido.getId().toString());

        if(pedido.getTransportista()!=null){
            NombreTrasnportista.setText(""+pedido.getTransportista().getNombres()+" "+pedido.getTransportista().getApellidos());
            PedidoCelular.setText(""+pedido.getTransportista().getCelular());
        }

        PedidoTxtStatus.setText(""+pedido.getEstado());
        DetalleSubtotal.setText("$"+pedido.getCostoVenta());
        DetalleCostoEnvio.setText("$"+pedido.getCostoEnvio());
        PedidoFecha.setText(pedido.getFechaRegistro());
        DetalleTotal.setText("$"+pedido.getTotal());
        if(pedido.getEstado().equals("ENTREGADA"))PedidoStatus.setBackground(getResources().getDrawable(R.drawable.border_estatus_purpura));
        if(pedido.getEstado().equals("WAITING")) PedidoStatus.setBackground(getResources().getDrawable(R.drawable.border_estatus_naranja));
        if(pedido.getEstado().equals("IN_PROGRESS")) PedidoStatus.setBackground(getResources().getDrawable(R.drawable.border_estatus_rojo));

    }

    private void  iniciar_recycler(){
        recyclerView=vista.findViewById(R.id.Recycler_Detalles);
        adapter= new VistasDetallePedido(LstPro);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
    private void peticion_pedidos(){
        Log.e("Pedidos","Detalles");
        retrofit = RetrofitCliente.getInstance();
        retrofitApi = retrofit.create(ApiService.class);
        Disposable disposable;
        disposable = (Disposable) retrofitApi.VerDetallePedidos( id_pedido, "FULL")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Response<ResponseDetallesPedidos>>() {
                    @Override
                    public void onNext(Response<ResponseDetallesPedidos> response) {


                        if(response.isSuccessful()){
                            pedido=response.body();
                            Log.e("Detalles",Global.convertObjToString(pedido));

                            LstPro.addAll(response.body().getDetallesPS());
                            continuar=true;
                        }else{
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
                        Log.e("CodPetiEroor",""+e.toString());

                    }

                    @Override
                    public void onComplete() {

                        Log.e("CodPetiEroor","completado");
                        // adapter.notifyDataSetChanged();
                        if(getActivity()==null || isRemoving() || isDetached()){
                            Log.e("activity","removido ");
                            return;
                        }else{



                            if(continuar){
                                iniciar_recycler();
                                llenarDatos();
                            }
                            else{
                                Toast.makeText(getActivity(),mensaje,Toast.LENGTH_LONG).show();
                            }





                        }



                    }
                });
    }


}
