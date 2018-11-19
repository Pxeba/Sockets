import GameManager.Companion.Point
import GameManager.Companion.Orientations
import GameManager.Companion.Ships
import GameManager.Companion.getShipEndpoint

class ValidationManager {
    companion object {

        @JvmStatic
        fun userInputValidation(inputSeq: String): Boolean {
            if(inputSeq.count() == 2) {
                return checkValidSeaPosition(Point(Client.Integer(inputSeq[0]), Client.Integer(inputSeq[1])))
            }
            return false
        }


        @JvmStatic
        fun checkValidSeaPosition(point: Point): Boolean {
            if(point.row in 0..9 && point.column in 0..9) {
                return true
            }
            return false
        }

        @JvmStatic
        fun checkIfPointHaveShip(point: Int?): Boolean { //S/C
            if(point == null) {
                return false
            }
            return true
        }

        @JvmStatic
        fun validateShipArea(initialPoint: Point, orient: Orientations?, ship: Ships, sea: Array<Array<Int?>>): Boolean {
            val endpoint = getShipEndpoint(initialPoint, orient, ship)
            if(!validateShipEndpoint(endpoint)) { return false}

            for(i in initialPoint.row..endpoint.row)
                for(j in initialPoint.column..endpoint.column)
                    if(sea[i][j] != null) { return false }

            return true
        }

        @JvmStatic
        fun validateShipEndpoint(endpoint: Point): Boolean {
            return when(checkValidSeaPosition(endpoint)) {
                true -> true
                false -> false
            }
        }
    }
}