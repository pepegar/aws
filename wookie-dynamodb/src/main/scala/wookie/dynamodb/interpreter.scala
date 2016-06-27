package wookie
package dynamodb

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.data.{ Xor, Kleisli }
import cats.~>
import com.amazonaws.AmazonWebServiceRequest
import com.amazonaws.AmazonWebServiceResponse
import com.amazonaws.Request
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.http.HttpResponseHandler
import com.amazonaws.transform.Marshaller
import scala.concurrent.Future

object interpreter extends Interpreter {
  import ast._
  import signer._
  import DynamoDB._
  import result._

  def serviceName: String = "dynamodb"

  def dynamoDBInterpreter(endpoint: String)(
    implicit
    system: ActorSystem,
    mat: ActorMaterializer
  ) = new (DynamoDBOp ~> Result) {
    def apply[A](command: DynamoDBOp[A]): Result[A] =
      Kleisli { signer: Signer[A] =>
        send(endpoint, signer.sign(command.req))(command.responseHandler, system, mat)
      }
  }

}
