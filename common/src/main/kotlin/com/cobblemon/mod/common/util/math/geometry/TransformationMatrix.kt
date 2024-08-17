/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.math.geometry

import com.cobblemon.mod.common.util.collections.ImmutableArray
import com.cobblemon.mod.common.util.collections.immutableArrayOf
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

/**
 * A 4x4 matrix that is used to represent transformations in three dimensional space.
 * For more information about the operations provided here, see
 * [Spatial Transformation Matrices](https://www.brainvoyager.com/bv/doc/UsersGuide/CoordsAndTransforms/SpatialTransformationMatrices.html)
 *
 * @property values the values of the matrix, in the form of a two-dimensional immutable array
 * @author landonjw
 */
data class TransformationMatrix internal constructor(val values: ImmutableArray<ImmutableArray<Float>>) {

    operator fun get(row: Int): ImmutableArray<Float> = values[row]

    operator fun times(right: TransformationMatrix): TransformationMatrix = combine(this, right)
    operator fun times(point: GeometricPoint): GeometricPoint = transform(this, point)
    operator fun times(normal: GeometricNormal): GeometricNormal = transform(this, normal)
    operator fun times(point: Vec3): Vec3 = transform(this, GeometricPoint(point)).toVec3d()

    override fun toString(): String {
        return """
            ${this[0][0]} ${this[0][1]} ${this[0][2]} ${this[0][3]}
            ${this[1][0]} ${this[1][1]} ${this[1][2]} ${this[1][3]}
            ${this[2][0]} ${this[2][1]} ${this[2][2]} ${this[2][3]}
            ${this[3][0]} ${this[3][1]} ${this[3][2]} ${this[3][3]}
        """.trimIndent()
    }

    companion object {

        /**
         * Gets the [identity matrix](https://en.wikipedia.org/wiki/Identity_matrix) for a matrix of size 4x4.
         *
         * @return a new instance of an identity matrix
         */
        val identityMatrix: TransformationMatrix = TransformationMatrix(identityArray().toImmutable())

        private fun identityArray() = arrayOf(
            arrayOf(1f, 0f, 0f, 0f),
            arrayOf(0f, 1f, 0f, 0f),
            arrayOf(0f, 0f, 1f, 0f),
            arrayOf(0f, 0f, 0f, 1f)
        )

        fun of(translation: GeometricPoint, rotation: Vector3f): TransformationMatrix {
            return translate(translation) * rotate(rotation)
        }

        /**
         * Gets the transformation matrix for a given set of translation values.
         * If a target transformation matrix is specified, the transformation matrices are concatenated.
         *
         * @param point the values to translate by
         * @param matrix an optional matrix to concatenate to
         *
         * @return a new transformation matrix for a given translation
         */
        fun translate(point: GeometricPoint, matrix: TransformationMatrix? = null): TransformationMatrix {
            val values = identityArray()
            values[0][3] = point.x
            values[1][3] = point.y
            values[2][3] = point.z
            val transformation = TransformationMatrix(values.toImmutable())
            return if (matrix == null) transformation else matrix * transformation
        }

        /**
         * Gets the rotation matrix for a given axis and angle.
         * If a target transformation matrix is specified, the transformation matrices are concatenated.
         *
         * If rotating by multiple axes, this should be called several times for each axis.
         * When doing so, keep in mind multiplication is non-commutative and order does matter.
         *
         * @param angle the angle to rotate by in radians
         *              positive values for clockwise, negative values for counter-clockwise
         * @param axis  the axis to rotate on
         * @param matrix an optional matrix to concatenate to
         *
         * @return a new transformation matrix for a given rotation
         */
        fun rotate(angle: Float, axis: Axis, matrix: TransformationMatrix? = null): TransformationMatrix {
            val transformation = axis.getRotationMatrix(angle)
            return if (matrix == null) transformation else matrix * transformation
        }

        /**
         * Gets a transformation matrix for a combination of rotation values.
         * This will rotate among all three axes.
         * **These are multiplied in the order Z, Y, X.**
         *
         * @param angles the angles to rotate each axis, in radians
         *               positive values for clockwise, negative values for counter-clockwise
         * @param matrix an optional matrix to concatenate to
         *
         * @return a new transformation matrix for the given rotations
         */
        fun rotate(angles: Vector3f, matrix: TransformationMatrix? = null): TransformationMatrix {
            val rotationX = Axis.X_AXIS.getRotationMatrix(angles.x)
            val rotationY = Axis.Y_AXIS.getRotationMatrix(angles.y)
            val rotationZ = Axis.Z_AXIS.getRotationMatrix(angles.z)
            val transformation = rotationZ * rotationY * rotationX
            return if (matrix == null) transformation else matrix * transformation
        }

        /**
         * Gets the transformation matrix for a given scalar.
         * If a target transformation matrix is specified, the transformation matrices are concatenated.
         *
         * @param scalar the scalar to get matrix for
         * @param matrix an optional matrix to concatenate to
         *
         * @return a new transformation matrix for a given scalar
         */
        fun scale(scalar: Float, matrix: TransformationMatrix? = null): TransformationMatrix =
            scale(scalar, scalar, scalar, matrix)

        /**
         * Gets the transformation matrix for a set of scalars.
         * These scalars allow you to stretch the dimensions of different dimensions.
         * If a target transformation matrix is specified, the transformation matrices are concatenated.
         *
         * @param scalarX the scalar for the x axis
         * @param scalarY the scalar for the y axis
         * @param scalarZ the scalar for the z axis
         * @param matrix an optional matrix to concatenate to
         *
         * @return a new transformation matrix for a given scalars
         */
        fun scale(
            scalarX: Float,
            scalarY: Float,
            scalarZ: Float,
            matrix: TransformationMatrix? = null
        ): TransformationMatrix {
            val values = identityArray()
            values[0][0] = scalarX
            values[1][1] = scalarY
            values[2][2] = scalarZ
            val transformation = TransformationMatrix(values.toImmutable())
            return if (matrix == null) transformation else matrix * transformation
        }

        /**
         * Transforms a three dimensional point to get the new position, based off of the given transformation
         * matrix.
         *
         * @param matrix the transformation matrix to use to transform the point
         * @param point the point to transform
         *
         * @return a new three dimensional point that has been transformed by the given matrix
         */
        fun transform(matrix: TransformationMatrix, point: GeometricPoint): GeometricPoint {
            val x = matrix[0][0] * point.x + matrix[0][1] * point.y + matrix[0][2] * point.z + matrix[0][3]
            val y = matrix[1][0] * point.x + matrix[1][1] * point.y + matrix[1][2] * point.z + matrix[1][3]
            val z = matrix[2][0] * point.x + matrix[2][1] * point.y + matrix[2][2] * point.z + matrix[2][3]
            return GeometricPoint(x, y, z)
        }

        /**
         * Transforms a normal to get the new position, based off of the given transformation matrix.
         *
         * @param matrix the transformation matrix to use to transform the normal
         * @param point the point to transform
         *
         * @return a new normal that has been transformed by the given matrix
         */
        fun transform(matrix: TransformationMatrix, point: GeometricNormal): GeometricNormal {
            val x = matrix[0][0] * point.x + matrix[0][1] * point.y + matrix[0][2] * point.z
            val y = matrix[1][0] * point.x + matrix[1][1] * point.y + matrix[1][2] * point.z
            val z = matrix[2][0] * point.x + matrix[2][1] * point.y + matrix[2][2] * point.z
            return GeometricNormal(x, y, z)
        }

        /**
         * Inverts the given transformation matrix, if possible.
         *
         * @param matrix the transformation matrix to invert
         * @return a new transformation matrix if successfully inverted, or null if inversion was not possible
         */
        fun invert(matrix: TransformationMatrix): TransformationMatrix? {
            val determinant = determinant(matrix)
            if (determinant == 0f) return null

            val inverseDeterminant = 1 / determinant

            val t00: Float = determinant3x3(
                arrayOf(
                    arrayOf(matrix[1][1], matrix[2][1], matrix[3][1]),
                    arrayOf(matrix[1][2], matrix[2][2], matrix[3][2]),
                    arrayOf(matrix[1][3], matrix[2][3], matrix[3][3])
                )
            )
            val t01: Float = -determinant3x3(
                arrayOf(
                    arrayOf(matrix[0][1], matrix[2][1], matrix[3][1]),
                    arrayOf(matrix[0][2], matrix[2][2], matrix[3][2]),
                    arrayOf(matrix[0][3], matrix[2][3], matrix[3][3])
                )
            )
            val t02: Float = determinant3x3(
                arrayOf(
                    arrayOf(matrix[0][1], matrix[1][1], matrix[3][1]),
                    arrayOf(matrix[0][2], matrix[1][2], matrix[3][2]),
                    arrayOf(matrix[0][3], matrix[1][3], matrix[3][3])
                )
            )
            val t03: Float = -determinant3x3(
                arrayOf(
                    arrayOf(matrix[0][1], matrix[1][1], matrix[2][1]),
                    arrayOf(matrix[0][2], matrix[1][2], matrix[2][2]),
                    arrayOf(matrix[0][3], matrix[1][3], matrix[2][3])
                )
            )
            val t10: Float = -determinant3x3(
                arrayOf(
                    arrayOf(matrix[1][0], matrix[2][0], matrix[3][0]),
                    arrayOf(matrix[1][2], matrix[2][2], matrix[3][2]),
                    arrayOf(matrix[1][3], matrix[2][3], matrix[3][3])
                )
            )
            val t11: Float = determinant3x3(
                arrayOf(
                    arrayOf(matrix[0][0], matrix[2][0], matrix[3][0]),
                    arrayOf(matrix[0][2], matrix[2][2], matrix[3][2]),
                    arrayOf(matrix[0][3], matrix[2][3], matrix[3][3])
                )
            )
            val t12: Float = -determinant3x3(
                arrayOf(
                    arrayOf(matrix[0][0], matrix[1][0], matrix[3][0]),
                    arrayOf(matrix[0][2], matrix[1][2], matrix[3][2]),
                    arrayOf(matrix[0][3], matrix[1][3], matrix[3][3])
                )
            )
            val t13: Float = determinant3x3(
                arrayOf(
                    arrayOf(matrix[0][0], matrix[1][0], matrix[2][0]),
                    arrayOf(matrix[0][2], matrix[1][2], matrix[2][2]),
                    arrayOf(matrix[0][3], matrix[1][3], matrix[2][3])
                )
            )
            val t20: Float = determinant3x3(
                arrayOf(
                    arrayOf(matrix[1][0], matrix[2][0], matrix[3][0]),
                    arrayOf(matrix[1][1], matrix[2][1], matrix[3][1]),
                    arrayOf(matrix[1][3], matrix[2][3], matrix[3][3])
                )
            )
            val t21: Float = -determinant3x3(
                arrayOf(
                    arrayOf(matrix[0][0], matrix[2][0], matrix[3][0]),
                    arrayOf(matrix[0][1], matrix[2][1], matrix[3][1]),
                    arrayOf(matrix[0][3], matrix[2][3], matrix[3][3])
                )
            )
            val t22: Float = determinant3x3(
                arrayOf(
                    arrayOf(matrix[0][0], matrix[1][0], matrix[3][0]),
                    arrayOf(matrix[0][1], matrix[1][1], matrix[3][1]),
                    arrayOf(matrix[0][3], matrix[1][3], matrix[3][3])
                )
            )
            val t23: Float = -determinant3x3(
                arrayOf(
                    arrayOf(matrix[0][0], matrix[1][0], matrix[2][0]),
                    arrayOf(matrix[0][1], matrix[1][1], matrix[2][1]),
                    arrayOf(matrix[0][3], matrix[1][3], matrix[2][3])
                )
            )
            val t30: Float = -determinant3x3(
                arrayOf(
                    arrayOf(matrix[1][0], matrix[2][0], matrix[3][0]),
                    arrayOf(matrix[1][1], matrix[2][1], matrix[3][1]),
                    arrayOf(matrix[1][2], matrix[2][2], matrix[3][2])
                )
            )
            val t31: Float = determinant3x3(
                arrayOf(
                    arrayOf(matrix[0][0], matrix[2][0], matrix[3][0]),
                    arrayOf(matrix[0][1], matrix[2][1], matrix[3][1]),
                    arrayOf(matrix[0][2], matrix[2][2], matrix[3][2])
                )
            )
            val t32: Float = -determinant3x3(
                arrayOf(
                    arrayOf(matrix[0][0], matrix[1][0], matrix[3][0]),
                    arrayOf(matrix[0][1], matrix[1][1], matrix[3][1]),
                    arrayOf(matrix[0][2], matrix[1][2], matrix[3][2])
                )
            )
            val t33: Float = determinant3x3(
                arrayOf(
                    arrayOf(matrix[0][0], matrix[1][0], matrix[2][0]),
                    arrayOf(matrix[0][1], matrix[1][1], matrix[2][1]),
                    arrayOf(matrix[0][2], matrix[1][2], matrix[2][2])
                )
            )

            val values = identityArray()
            values[0][0] = t00 * inverseDeterminant
            values[0][1] = t01 * inverseDeterminant
            values[0][2] = t02 * inverseDeterminant
            values[0][3] = t03 * inverseDeterminant
            values[1][0] = t10 * inverseDeterminant
            values[1][1] = t11 * inverseDeterminant
            values[1][2] = t12 * inverseDeterminant
            values[1][3] = t13 * inverseDeterminant
            values[2][0] = t20 * inverseDeterminant
            values[2][1] = t21 * inverseDeterminant
            values[2][2] = t22 * inverseDeterminant
            values[2][3] = t23 * inverseDeterminant
            values[3][0] = t30 * inverseDeterminant
            values[3][1] = t31 * inverseDeterminant
            values[3][2] = t32 * inverseDeterminant
            values[3][3] = t33 * inverseDeterminant

            return TransformationMatrix(values.toImmutable())
        }

        /**
         * Gets the [determinant](https://en.wikipedia.org/wiki/Determinant) of a given transformation matrix.
         *
         * @param matrix the transformation matrix to get determinant for
         * @return the determinant of the matrix
         */
        fun determinant(matrix: TransformationMatrix): Float {
            var determinant: Float = (matrix[0][0]
                    * (matrix[1][1] * matrix[2][2] * matrix[3][3] + matrix[2][1] * matrix[3][2] * matrix[1][3] + matrix[3][1] * matrix[1][2] * matrix[2][3]
                    - matrix[3][1] * matrix[2][2] * matrix[1][3] - matrix[1][1] * matrix[3][2] * matrix[2][3] - matrix[2][1] * matrix[1][2] * matrix[3][3]))
            determinant -= (matrix[1][0]
                    * (matrix[0][1] * matrix[2][2] * matrix[3][3] + matrix[2][1] * matrix[3][2] * matrix[0][3] + matrix[3][1] * matrix[0][2] * matrix[2][3]
                    - matrix[3][1] * matrix[2][2] * matrix[0][3] - matrix[0][1] * matrix[3][2] * matrix[2][3] - matrix[2][1] * matrix[0][2] * matrix[3][3]))
            determinant += (matrix[2][0]
                    * (matrix[0][1] * matrix[1][2] * matrix[3][3] + matrix[1][1] * matrix[3][2] * matrix[0][3] + matrix[3][1] * matrix[0][2] * matrix[1][3]
                    - matrix[3][1] * matrix[1][2] * matrix[0][3] - matrix[0][1] * matrix[3][2] * matrix[1][3] - matrix[1][1] * matrix[0][2] * matrix[3][3]))
            determinant -= (matrix[3][0]
                    * (matrix[0][1] * matrix[1][2] * matrix[2][3] + matrix[1][1] * matrix[2][2] * matrix[0][3] + matrix[2][1] * matrix[0][2] * matrix[1][3]
                    - matrix[2][1] * matrix[1][2] * matrix[0][3] - matrix[0][1] * matrix[2][2] * matrix[1][3] - matrix[1][1] * matrix[0][2] * matrix[2][3]))
            return determinant
        }

        private fun determinant3x3(values: Array<Array<Float>>): Float {
            assert(values.size == 3)
            for (row in values) assert(row.size == 3)

            return values[0][0] * (values[1][1] * values[2][2] - values[1][2] * values[2][1]) +
                    values[0][1] * (values[1][2] * values[2][0] - values[1][0] * values[2][2]) +
                    values[0][2] * (values[1][0] * values[2][1] - values[1][1] * values[2][0])
        }

        /**
         * Combines, or concatenates, two transformation matrices.
         *
         * This operation is completed by matrix multiplication between the two matrices.
         * When using this, keep in mind it is non-commutative and order does matter.
         *
         * @param left the left side of the combination
         * @param right the right side of the combination
         *
         * @return a new transformation matrix representing the combination of the original matrices
         */
        fun combine(left: TransformationMatrix, right: TransformationMatrix): TransformationMatrix {
            val values = Array(4) {
                Array(4) { 0f }
            }

            for (row in 0 until 4) {
                for (col in 0 until 4) {
                    values[row][col] = multiplyRowCol(row, col, left, right)
                }
            }
            return TransformationMatrix(values.toImmutable())
        }

        private fun multiplyRowCol(row: Int, col: Int, left: TransformationMatrix, right: TransformationMatrix): Float {
            var sum = 0f
            for (i in 0 until 4) sum += left[row][i] * right[i][col]
            return sum
        }

        /**
         * Returns an immutable two-dimensional array.
         *
         * This assumes that the array that is being turned immutable is of 4x4.
         *
         * @return an immutable version of the two-dimensional array
         */
        private fun Array<Array<Float>>.toImmutable(): ImmutableArray<ImmutableArray<Float>> {
            return immutableArrayOf(
                immutableArrayOf(*this[0]),
                immutableArrayOf(*this[1]),
                immutableArrayOf(*this[2]),
                immutableArrayOf(*this[3])
            )
        }

    }

}