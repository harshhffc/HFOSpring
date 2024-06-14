package com.homefirstindia.hfo.repository.v1

import com.homefirstindia.hfo.model.v1.LocationDirections
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LocationDirectionsRepository: JpaRepository<LocationDirections, String>