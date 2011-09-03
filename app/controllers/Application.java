package controllers;

import models.Post;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.Before;
import play.mvc.Controller;

import java.util.List;

public class Application extends Controller {

    @Before
    static void addDefaults() {
        renderArgs.put("blogTitle", Play.configuration.getProperty("blog.title"));
        renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"));
    }

    public static void index() {
        final GenericModel.JPAQuery posts = Post.find("order by postedAt desc");

        Post frontPost = posts.first();
        List<Post> olderPosts = posts.from(1).fetch(10);

        render(frontPost, olderPosts);
    }

    public static void show(Long id) {
        Post post = Post.findById(id);
        String randomID = Codec.UUID();
        render(post, randomID);
    }

    public static void postComment(
            Long postId,
            @Required(message = "Author is required") String author,
            @Required(message = "A message is required") String content,
            @Required(message = "Please type the code") String code,
            String randomID) {
        Post post = Post.findById(postId);
        validation.equals(
                code, Cache.get(randomID)
        ).message("Invalid code. Please type it again");
        if (validation.hasErrors()) {
            render("Application/show.html", post, randomID);
        }
        post.addComment(author, content);
        flash.success("Thanks for posting %s", author);
        Cache.delete(randomID);
        show(postId);
    }

    public static void captcha(String id) {
        Images.Captcha captcha = Images.captcha();
        String code = captcha.getText("#E4EAFD");
        Cache.set(id, code, "10mn");
        renderBinary(captcha);
    }
}