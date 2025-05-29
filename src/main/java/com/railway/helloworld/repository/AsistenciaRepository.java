package com.railway.helloworld.repository;


import com.railway.helloworld.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    //Aqui creamos una lista que contendra los registros de fecha, recibe como parametro los registro que queremos que se
    //almacenen en la lista.
    List <Asistencia> findByAsistenciaFecha (LocalDate asistencia_fecha);

    List<Asistencia> findByAsistenciaFechaBetween(LocalDate inicio, LocalDate fin);

    List<Asistencia> findByUsuarioUsuarioIdAndAsistenciaFechaBetween(Long usuarioId, LocalDate fechaInicio, LocalDate fechaFin);




}
