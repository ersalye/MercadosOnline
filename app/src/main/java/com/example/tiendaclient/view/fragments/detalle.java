package com.example.tiendaclient.view.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tiendaclient.R;
import com.example.tiendaclient.adapter.VistasDetalleProductos;
import com.example.tiendaclient.models.compra.Compra;
import com.example.tiendaclient.models.compra.CompraProductos;
import com.example.tiendaclient.models.compra.PuestosCompra;
import com.example.tiendaclient.utils.Global;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import jp.wasabeef.recyclerview.animators.ScaleInRightAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
public class detalle extends Fragment {
    View vista;
    TextView DetaCancelarPeido, DetaSubtotal, DetaCostoEnvio, DetaTotal, DetaTotal2;
    RelativeLayout DetaContinuar;
    List<CompraProductos> LstPro = new ArrayList<>();
    public String id_del_fragment;
    ScaleInRightAnimator animator1 = new ScaleInRightAnimator();
    RecyclerView recyclerView;
    VistasDetalleProductos adapter;



    SweetAlertDialog dialog_permisos;
    SweetAlertDialog dialog_manual;

    public Compra CompraNueva = new Compra();

    Double CostoEnvi = 2.00;

    public int PosicionListaArray = 0;

    public detalle() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vista = inflater.inflate(R.layout.fragment_detalle, container, false);
        return vista;


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        UI();
        unir();
        llenar_Detalle();
        iniciar_recycler();
        Click();


    }

    private void UI() {

        DetaCancelarPeido = vista.findViewById(R.id.DetalleCancelarPedido);
        DetaSubtotal = vista.findViewById(R.id.DetalleSubtotal);
        DetaCostoEnvio = vista.findViewById(R.id.DetalleCostoEnvio);
        DetaTotal = vista.findViewById(R.id.DetalleTotal);
        DetaTotal2 = vista.findViewById(R.id.DetalleTotal2);
        DetaContinuar = vista.findViewById(R.id.DetalleBtnContinuar);

    }

    private void llenar_Detalle() {
        CompraNueva=Global.VerCompras.get(PosicionListaArray);
        DetaSubtotal.setText("$" + CompraNueva.getTotal().toString());
        DetaCostoEnvio.setText("$" + Global.formatearDecimales(CostoEnvi,2));
        DetaTotal.setText("$" + Global.formatearDecimales((CompraNueva.getTotal() + CostoEnvi),2));
        DetaTotal2.setText("$" + Global.formatearDecimales((CompraNueva.getTotal() + CostoEnvi),2));

    }

    private void Click() {
        DetaCancelarPeido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.VerCompras.remove(PosicionListaArray);
                getFragmentManager().popBackStack();

            }
        });

        DetaContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if(validar_permisos()){
                    ir_ubicacion();
                }



            }
        });

    }


    private void unir() {
        LstPro.clear();
        for (PuestosCompra p : CompraNueva.getPuestos()) {
            LstPro.addAll(p.getProductos());

        }
    }

    private  void eliminar_producto(CompraProductos product){

        int indice_puesto=0;
        int indice_final=0;


        for (PuestosCompra p : Global.VerCompras.get(PosicionListaArray).getPuestos()) {


           if(p.getId()==product.getIdPuesto()){

               indice_final=indice_puesto;
           }
                indice_puesto++;
        }


        Global.VerCompras.get(PosicionListaArray).getPuestos().get(indice_final).getProductos().remove(product);

        Global.VerCompras.get(PosicionListaArray).setCantidad(Global.VerCompras.get(PosicionListaArray).getCantidad()-product.getId_cantidad());
        Global.VerCompras.get(PosicionListaArray).setTotal(Global.formatearDecimales((Global.VerCompras.get(PosicionListaArray).getTotal()-product.getTotal()),2));

        if(Global.VerCompras.get(PosicionListaArray).getCantidad()<=0){

            Global.VerCompras.remove(PosicionListaArray);
            getFragmentManager().popBackStack();
        }else{
            llenar_Detalle();
        }



        Log.e("removido", Global.convertObjToString(Global.VerCompras));

    }



    @Override
    public void postponeEnterTransition() {
        super.postponeEnterTransition();
    }

    private void  iniciar_recycler(){
        recyclerView=vista.findViewById(R.id.Recycler_Detalles);
        adapter= new VistasDetalleProductos(LstPro, getFragmentManager(), id_del_fragment, new VistasDetalleProductos.OnItemLongClicListener() {
            @Override
            public void onItemClickLong(CompraProductos product, int position) {
                borrar_producto(position);
                eliminar_producto(product);
            }
        });
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


    private void borrar_producto(int position){
        animator1.setRemoveDuration(1000);
        recyclerView.setItemAnimator(animator1);
        // eliminar("http://learn4win.com/WebServices/eliminar_alarma.php",malarma.get(position));
        LstPro.remove(position);
        adapter.notifyItemRemoved(position);
    }
///////////////////////////////////////////////////////////////////////////////
private void ir_ubicacion(){
    ubica_entrega ubi = new ubica_entrega();
    ubi.id_del_fragment=id_del_fragment;
    ubi.PosicionListaArray=PosicionListaArray;
    ubi.productos=LstPro;
    ubi.Total_precio=Global.formatearDecimales((CompraNueva.getTotal() + CostoEnvi),2);

    FragmentTransaction fragmentTransaction;
    fragmentTransaction = getFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.Contenedor_Fragments, ubi).addToBackStack("frag_ubi");
    fragmentTransaction.commit();

}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length == 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.e("permisos ", "defecto");

                ir_ubicacion();
            } else {
                Log.e("permisos", "manual");

                solicitarPermisosManual();
            }
        }

    }


private boolean validar_permisos() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return true;
    }
    if (ActivityCompat.checkSelfPermission(getActivity().getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        Log.e("tengo permisos", "bien");
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
            //Todo no tiene permisos y le sale para dar
            cargarDialogoRecomendacion();
            Log.e("dialogo", "recomendacion");
        } else {
            //Todo no tiene permisos por que los nego y puso no volver a presentar asi que  mandamos de nuevo  a pedir y entrara
            //Todo a permisos manual
            //  requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
           requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            Log.e("dialogo", "else");
        }
        return false;
    }
    return true;
}
    //Todo un dialog que recomienda por que activar los permisos

    private void cargarDialogoRecomendacion() {

        dialog_permisos = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE);
        dialog_permisos.setTitleText("Permisos Desactivados");
        dialog_permisos.setContentText("Debe aceptar los permisos para el correcto funcionamiento de la App");
        dialog_permisos.setConfirmText("OK2");
        dialog_permisos.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    requestPermissions
                            (new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                }
                dialog_permisos.dismissWithAnimation();
            }
        });


        dialog_permisos.setCancelable(false);
        dialog_permisos.show();
    }

    private void solicitarPermisosManual() {
        dialog_manual = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
        dialog_manual.setTitleText("Permisos Desactivados");
        dialog_manual.setContentText(" Configure los permisos de forma manual para el correcto funcionamiento de la App");
        dialog_manual.setConfirmText("OK");
        dialog_manual.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);

                dialog_manual.dismissWithAnimation();
            }
        });


        dialog_manual.setCancelable(false);
        dialog_manual.show();


    }



}