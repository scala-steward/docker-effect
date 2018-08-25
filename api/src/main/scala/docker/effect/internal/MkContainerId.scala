package docker.effect.internal

import docker.effect.types.Container
import docker.effect.util.CirceCodecs.mappedDecoderWithError
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.Decoder

object MkContainerId extends newtype[NonEmptyString] with ContainerIdCodecs

sealed private[internal] trait ContainerIdCodecs {

  implicit val containerIdDecoder: Decoder[Container.Id] =
    mappedDecoderWithError(
      NonEmptyString.from,
      Container.Id.apply,
      _ => "Cannot decode an empty string to a container id"
    )
}