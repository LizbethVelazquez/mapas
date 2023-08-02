package com.example.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.map.databinding.ActivityMapsBinding
import com.example.map.databinding.ActivityPlacesBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

private lateinit var json: String
class Places : AppCompatActivity() {
    private lateinit var binding: ActivityPlacesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Escuelas Chiconquiaco"
        val adapter = SAdapter(SAdapter.OnClickListener { p ->
            val int = Intent(this@Places, MapsActivity::class.java)
            int.putExtra("name",p.name)
            int.putExtra("lat",p.lat)
            int.putExtra("lng",p.lng)
            this@Places.startActivity(int)
            //this@Places.finish()
        })

        binding.rv.adapter = adapter
        adapter.submitList(getData())
        binding.rv.layoutManager = LinearLayoutManager(this)

    }



    private fun getData(): ArrayList<Place>{
        try{
            json = applicationContext.assets.open("jsons/escuelas.json")
                .bufferedReader()
                .use { it.readText() }
        }catch (e: IOException){
            Log.e("PLACES",e.stackTraceToString())
        }
        val list = object : TypeToken<ArrayList<Place>>() {}.type
        return Gson().fromJson(json,list)
    }
}