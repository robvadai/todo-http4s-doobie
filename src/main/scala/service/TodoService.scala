package service

import cats.effect.IO
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import model.{Importance, SourceCode, SourceCodeExistsError, SourceCodeLookup, SourceCodeNotFound, SourceCodeNotFoundError, SourceCodeVerificationResponse, Todo, TodoNotFoundError}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri}
import repository.{SourceCodeRepository, TodoRepository}

import scala.util.Try

class SourceCodeService(repository: SourceCodeRepository) extends Http4sDsl[IO] {
  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "sourceCode" / sourceCode =>
      for {
        getResult <- repository.getSourceCode(sourceCode)
        response <- sourceCodeResult(getResult)
      } yield response
    case req @ POST -> Root / "sourceCode" / "verify" =>
      for {
        sourceCode <- req.decodeJson[SourceCodeLookup]
        exists <- repository.isNonExistentSourceCode(sourceCode)
        response <- sourceCodeVerificationResult(exists)
      } yield response
    case req @ POST -> Root / "sourceCode" / "create" =>
      for {
        sourceCode <- req.decodeJson[SourceCode]
        _ <- repository.createSourceCode(sourceCode)
        response <- Created()
      } yield response
  }

  private def sourceCodeVerificationResult(exists: Either[SourceCodeExistsError.type, SourceCodeNotFound.type]) = {
    exists match {
      case Left(_) => NotAcceptable()
      case Right(_) => Ok(SourceCodeVerificationResponse("aaa", "bbb", "ccc", "ddd" :: "eee" :: Nil, "fff", "ggg").asJson)
    }
  }

  private def sourceCodeResult(result: Either[SourceCodeNotFoundError.type, SourceCodeLookup]) = {
    result match {
      case Left(SourceCodeNotFoundError) => NotFound()
      case Right(sourceCode) => Ok(sourceCode.asJson)
    }
  }
}

class TodoService(repository: TodoRepository) extends Http4sDsl[IO] {
  private implicit val encodeImportance: Encoder[Importance] = Encoder.encodeString.contramap[Importance](_.value)

  private implicit val decodeImportance: Decoder[Importance] = Decoder.decodeString.map[Importance](Importance.unsafeFromString)

  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "todos" =>
      Ok(Stream("[") ++ repository.getTodos.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.application.json))

    case GET -> Root / "todos" / LongVar(id) =>
      for {
        getResult <- repository.getTodo(id)
        response <- todoResult(getResult)
      } yield response

    case req @ POST -> Root / "todos" =>
      for {
        todo <- req.decodeJson[Todo]
        createdTodo <- repository.createTodo(todo)
        response <- Created(createdTodo.asJson, Location(Uri.unsafeFromString(s"/todos/${createdTodo.id.get}")))
      } yield response

    case req @ PUT -> Root / "todos" / LongVar(id) =>
      for {
        todo <-req.decodeJson[Todo]
        updateResult <- repository.updateTodo(id, todo)
        response <- todoResult(updateResult)
      } yield response

    case DELETE -> Root / "todos" / LongVar(id) =>
      repository.deleteTodo(id).flatMap {
        case Left(TodoNotFoundError) => NotFound()
        case Right(_) => NoContent()
      }
  }

  private def todoResult(result: Either[TodoNotFoundError.type, Todo]) = {
    result match {
      case Left(TodoNotFoundError) => NotFound()
      case Right(todo) => Ok(todo.asJson)
    }
  }
}
