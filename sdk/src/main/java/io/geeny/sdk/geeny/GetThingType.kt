package io.geeny.sdk.geeny

import io.geeny.sdk.geeny.cloud.api.repos.MessageType
import io.geeny.sdk.geeny.cloud.api.repos.Resource
import io.geeny.sdk.geeny.cloud.api.repos.ThingType
import io.geeny.sdk.geeny.cloud.api.repos.messagetype.MessageTypeRepository
import io.geeny.sdk.geeny.cloud.api.repos.resource.ThingTypeRepository
import io.geeny.sdk.geeny.things.ResourceMethod
import io.geeny.sdk.geeny.things.TheResource
import io.geeny.sdk.geeny.things.TheThingType
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class GetThingType(
        private val thingTypeRepository: ThingTypeRepository,
        private val messageTypeRepository: MessageTypeRepository
) {

    fun get(thingTypeId: String): Observable<TheThingType> =
            thingTypeRepository.getOrDownload(thingTypeId)
                    .doOnNext { "ThingType loaded $it" }
                    .flatMap {
                        Observable.zip<ThingType, List<TheResource>, TheThingType>(
                                Observable.just(it),
                                getResources(it.resources),
                                BiFunction { thingType, resourceList ->
                                    TheThingType(thingType, resourceList)
                                }

                        )
                    }

    private fun getResources(resources: List<Resource>): Observable<List<TheResource>> =
            Observable.fromIterable(resources)
                    .flatMap {
                        Observable.zip<Resource, MessageType, TheResource>(
                                Observable.just(it),
                                messageTypeRepository.get(it.messageType),
                                BiFunction { resource, messageType ->
                                    TheResource(
                                            resource.uri,
                                            resource.uri,
                                            ResourceMethod.from(resource.method),
                                            messageType)
                                }
                        )
                    }
                    .toList().toObservable()


}