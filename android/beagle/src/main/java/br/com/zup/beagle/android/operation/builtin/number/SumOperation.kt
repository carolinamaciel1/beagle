/*
 * Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.beagle.android.operation.builtin.number

import br.com.zup.beagle.android.operation.Operation
import br.com.zup.beagle.android.operation.builtin.toListOfDoubles
import br.com.zup.beagle.android.operation.builtin.toListOfInts
import br.com.zup.beagle.annotation.RegisterOperation

@RegisterOperation("sum")
internal class SumOperation : Operation {

    override fun execute(vararg params: Any?): Number {
        return if (params[0] is Int) {
            params.toListOfInts().sum()
        } else {
            params.toListOfDoubles().sum()
        }
    }
}