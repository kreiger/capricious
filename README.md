# Capricious
## Filtering Twitch Chat Viewer written in Java

Following the Twitch chat of a channel during a popular event can be really hard.

Capricious is a simple Twitch chat viewer that will filter out the duplicate messages.

## How to use

1. Build Capricious with `./gradlew build`
2. Run Capricious with `java -jar build/libs/capricious.jar` or `./gradlew run`.
3. You will be asked for the name of a Twitch channel, e.g. "GamesDoneQuick".
4. Press OK.
5. You're now viewing the Twitch Chat for that channel, without the duplicate messages.


## License

Copyright 2017 Christoffer Hammarström

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
