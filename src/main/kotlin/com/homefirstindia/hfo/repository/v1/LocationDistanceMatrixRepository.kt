package com.homefirstindia.hfo.repository.v1

import com.homefirstindia.hfo.model.v1.LocationDistanceMatrix
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LocationDistanceMatrixRepository: JpaRepository<LocationDistanceMatrix, String>