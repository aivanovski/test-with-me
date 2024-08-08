package com.github.aivanovski.testswithme.web.entity

import com.github.aivanovski.testswithme.web.data.database.converters.FsPathConverter
import com.github.aivanovski.testswithme.web.data.database.converters.UidConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "Flows")
data class Flow(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(name = "uid")
    @Convert(converter = UidConverter::class)
    val uid: Uid,

    @Column(name = "project_uid")
    @Convert(converter = UidConverter::class)
    val projectUid: Uid,

    @Column(name = "group_uid", nullable = true)
    @Convert(converter = UidConverter::class)
    val groupUid: Uid,

    @Column(name = "name")
    val name: String,

    @Column(name = "path")
    @Convert(converter = FsPathConverter::class)
    val path: FsPath
) {
    // TODO: Add db fields
}