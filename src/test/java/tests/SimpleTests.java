package tests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.*;
import java.util.function.BiFunction;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

//import org.testng.annotations.Test;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimpleTests {
    static Integer id = new Random().nextInt(1000, 100000);

    @Test
    @Order(0)
    public void Petstore_CreatePet_ShouldReturn200() {
        var factory = new BodyFactory();
        var body = factory.CreateDefaultBodyBuilder().build();

        var request = RequestHelper.SendPost("/pet", body);
        request.then().statusCode(200);

        var response = request.getBody().as(DefaultBody.class);

        CreateDefaultAsserPipe()
                .run(body, response);
    }

    @Test
    @Order(1)
    public void Petstore_Get_AlreadyCreatedPet_ShouldReturn200() {
        var builder = new BodyFactory().CreateDefaultBodyBuilder();
        var model = builder.build();

        var request = RequestHelper.SendGet("/pet/{id}", id);
        request.then().statusCode(200);

        var response = request.getBody().as(DefaultBody.class);

        CreateDefaultAsserPipe()
                .run(model, response);
    }

    @Test
    @Order(2)
    public void Petstore_Update_CreatedPet_ShouldReturn200() {
        var builder = new BodyFactory().CreateDefaultBodyBuilder();
        var body = builder.withStatus("sold").build();

        var request = RequestHelper.SendPut("/pet", body);
        request.then().statusCode(200);

        var response = request.getBody().as(DefaultBody.class);

        CreateDefaultAsserPipe()
                .run(body, response);
    }

    @Test
    @Order(3)
    public void Petstore_Get_UnrealPet_ShouldReturn404() {


        var params = new GetPetParams( "111231231231231231");
        var request = RequestHelper.SendGet("/pet/{id}",params.id);
        request.then().statusCode(404);
        var error = request.getBody().as(ErrorBody.class);

        CreateErrorAssertPipe(1,"error","not found")
                .run(params,error);
    }

    @Test
    @Order(4)
    public void Petstore_Get_TooBigId_ShouldReturn404() {
        var params = new GetPetParams("111111123123123123123123213123321");
        var request = RequestHelper.SendGet("/pet/{id}",params.id);
        request.then().statusCode(404);
        var error = request.getBody().as(ErrorBody.class);

        CreateErrorAssertPipe(404,"unknown",params.id)
                .run(params,error);
    }

    @Test
    @Order(5)
    public void Petstore_Get_IncorrectId_ShouldReturn404() {
        var params = new GetPetParams("111111123123123123123123213aaaaaaaaaa");
        var request = RequestHelper.SendGet("/pet/{id}",params.id);
        request.then().statusCode(404);
        var error = request.getBody().as(ErrorBody.class);

        CreateErrorAssertPipe(404,"unknown",params.id)
                .run(params,error);
    }


    //Classes and methods that helps with testing )
    @BeforeAll
    public static void BuildRestAssured() {
        var requestBuilder = new RequestSpecBuilder()
                .setBaseUri("https://petstore.swagger.io/v2")
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL);
        RestAssured.requestSpecification = requestBuilder.build();

        var responseBuilder = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .log(LogDetail.ALL);
        RestAssured.responseSpecification = responseBuilder.build();
    }

    /*
    *  public int id;
        public String name;
        public String status;
        public List<Tag> tags = new ArrayList<>();
        public List<Tag> categories = new ArrayList<>();
        public List<String> photoUrls = new ArrayList<>();
    * */
    public AssertPipe<DefaultBody,DefaultBody> CreateDefaultAsserPipe() {
        return new AssertPipe<DefaultBody,DefaultBody>()
                .with("idEquals", (req, res) -> assertThat(res.id).isEqualTo(req.id))
                .with("nameEquals", (req, res) -> assertThat(res.name).isEqualTo(req.name))
                .with("statusEquals", (req, res) -> assertThat(res.status).isEqualTo(req.status))
                .with("photosEquals", (req, res) -> assertThat(res.photoUrls).containsAll(req.photoUrls))
                .with("categoriesEquals", (req, res) -> assertThat(res.category).isEqualTo(req.category))
                .with("TagsEquals", (req, res) -> assertThat(res.tags).isEqualTo(req.tags));
    }

    static class GetPetParams{
        public String id;

        public GetPetParams(String id){
            this.id = id;
        }
    }
    public AssertPipe<GetPetParams,ErrorBody> CreateErrorAssertPipe(
            int expectedErrorCode,
            String expectedErrorType,
            String message){
        return new AssertPipe<GetPetParams,ErrorBody>()
                .with("ErrorCodeEquals",(req,res) -> assertThat(expectedErrorCode).isEqualTo(res.code))
                .with("MessageShouldContain",(req,res) -> assertThat(res.message).contains(message))
                .with("ErrorTypeEquals",(req,res) -> assertThat(res.type).isEqualTo(expectedErrorType))
                ;
    }

    static class AssertPipe<TRequest,TResponse> {
        private final Map<String, BiFunction<TRequest, TResponse, org.assertj.core.api.Assert>> _pipe = new HashMap<>();

        public AssertPipe() {
        }

        public AssertPipe<TRequest,TResponse> with(String name, BiFunction<TRequest, TResponse, org.assertj.core.api.Assert> assertion) {
            _pipe.put(name, assertion);
            return this;
        }

        public AssertPipe<TRequest,TResponse> without(String name) {
            _pipe.remove(name);
            return this;
        }

        public void run(TRequest request, TResponse response) {
            for (var value : _pipe.values()) {
                value.apply(request, response);
            }
        }
    }

    static class RequestHelper {
        public static Response SendGet(String endpoint) {
            return when().get(endpoint);
        }

        public static Response SendGet(String endpoint, Object... params) {
            return when().get(endpoint, params);
        }

        public static Response SendPost(String endpoint, Object body) {
            return given().body(body).when().post(endpoint);
        }

        public static Response SendPut(String endpoint, Object body) {
            return given().body(body).when().put(endpoint);
        }
    }

    static class ErrorBody {
        public int code;
        public String type;
        public String message;

        public ErrorBody() {
        }
    }

    static class Tag {
        public int id;
        public String name;

        public Tag() {
            id = 0;
            name = "";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Tag tag)) return false;
            return id == tag.id && Objects.equals(name, tag.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
    }

    static class DefaultBody {

        public int id;
        public String name;
        public String status;
        public List<Tag> tags = new ArrayList<>();
        public Tag category;
        public List<String> photoUrls = new ArrayList<>();

        public DefaultBody() {
        }


    }

    static class BodyFactory {
        public DefaultBodyBuilder CreateDefaultBodyBuilder() {
            return new DefaultBodyBuilder()
                    .withId(id)
                    .withName("doggieNiki")
                    .withStatus("available")
                    .withTag(1, "myTag1")
                    .withCategory(1, "myCategory1")
                    .withPhotoUrl("https://www.google.com/url?sa=i&url=https%3A%2F%2Fria.ru%2F20170301%2F1488412994.html&psig=AOvVaw1Pa2W13e3QUJhlVQuRuyhA&ust=1680436469450000&source=images&cd=vfe&ved=0CBAQjRxqFwoTCMD83efPiP4CFQAAAAAdAAAAABAE");
        }
    }

    static class TagBuilder {
        Tag tag;

        public TagBuilder() {
            this.tag = new Tag();
        }

        public TagBuilder withId(int id) {
            this.tag.id = id;
            return this;
        }

        public TagBuilder withName(String name) {
            this.tag.name = name;
            return this;
        }

        public Tag build() {
            return tag;
        }
    }

    static class DefaultBodyBuilder {
        DefaultBody body;

        public DefaultBodyBuilder(DefaultBody body) {
            this.body = body;
        }

        public DefaultBodyBuilder() {
            this.body = new DefaultBody();
        }

        public DefaultBodyBuilder withId(int id) {
            body.id = id;
            return this;
        }

        public DefaultBodyBuilder withName(String name) {
            body.name = name;
            return this;
        }

        public DefaultBodyBuilder withStatus(String status) {
            body.status = status;
            return this;
        }

        public DefaultBodyBuilder withTag(int id, String name) {
            body.tags.add(
                    new TagBuilder()
                            .withId(id)
                            .withName(name)
                            .build()
            );
            return this;
        }

        public DefaultBodyBuilder withPhotoUrl(String url) {
            body.photoUrls.add(url);
            return this;
        }

        public DefaultBodyBuilder withCategory(int id, String name) {
            this.body.category = new TagBuilder().withId(id).withName(name).build();
            return this;
        }

        public DefaultBody build() {
            return body;
        }
    }
}
