package org.jetbrains.dokka.tests

import org.junit.Test
import kotlin.test.*
import com.jetbrains.dokka.*


public class PropertyTest {
    Test fun valueProperty() {
        verifyModel("test/data/properties/valueProperty.kt") { model ->
            with(model.nodes.single().members.single()) {
                assertEquals("property", name)
                assertEquals(DocumentationNodeKind.Property, kind)
                assertEquals("", doc)
                assertTrue(details.none())
                assertTrue(members.none())
                assertTrue(links.none())
            }
        }
    }

    Test fun variableProperty() {
        verifyModel("test/data/properties/variableProperty.kt") { model ->
            with(model.nodes.single().members.single()) {
                assertEquals("property", name)
                assertEquals(DocumentationNodeKind.Property, kind)
                assertEquals("", doc)
                assertTrue(details.none())
                assertTrue(members.none())
                assertTrue(links.none())
            }
        }
    }

    Test fun valuePropertyWithGetter() {
        verifyModel("test/data/properties/valuePropertyWithGetter.kt") { model ->
            with(model.nodes.single().members.single()) {
                assertEquals("property", name)
                assertEquals(DocumentationNodeKind.Property, kind)
                assertEquals("", doc)
                assertTrue(details.none())
                assertTrue(links.none())
                with(members.single()) {
                    assertEquals("<get-property>", name)
                    assertEquals(DocumentationNodeKind.Function, kind)
                    assertEquals("", doc)
                    assertTrue(details.none())
                    assertTrue(links.none())
                    assertTrue(members.none())
                }
            }
        }
    }

    Test fun variablePropertyWithAccessors() {
        verifyModel("test/data/properties/variablePropertyWithAccessors.kt") { model ->
            with(model.nodes.single().members.single()) {
                assertEquals("property", name)
                assertEquals(DocumentationNodeKind.Property, kind)
                assertEquals("", doc)
                assertTrue(details.none())
                assertTrue(links.none())

                assertEquals(2, members.count())
                with(members.elementAt(0)) {
                    assertEquals("<get-property>", name)
                    assertEquals(DocumentationNodeKind.Function, kind)
                    assertEquals("", doc)
                    assertTrue(details.none())
                    assertTrue(links.none())
                    assertTrue(members.none())
                }
                with(members.elementAt(1)) {
                    assertEquals("<set-property>", name)
                    assertEquals(DocumentationNodeKind.Function, kind)
                    assertEquals("", doc)
                    with(details.single()) {
                        assertEquals("value", name)
                        assertEquals(DocumentationNodeKind.Parameter, kind)
                        assertEquals("", doc)
                        assertTrue(details.none())
                        assertTrue(links.none())
                        assertTrue(members.none())
                    }
                    assertTrue(links.none())
                    assertTrue(members.none())
                }
            }
        }
    }
}
