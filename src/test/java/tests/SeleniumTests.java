package tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import static org.assertj.core.api.Assertions.*;

public class SeleniumTests {
    public static WebDriver _driver = null;
    @Test
    public void simpleTest(){
        _driver.get("https://playground.learnqa.ru/puzzle/triangle");

        //удостовериться что есть кнопка "я сдаюсь"
        var surrenderButton = _driver.findElement(By.xpath("//button[@id='show_answ' and text()[.='Я сдаюсь']]"));
        assertThat(surrenderButton).isNotNull();

        //кликнуть по кнопке
        surrenderButton.click();
        //проверить что появились элкменты
        //1. ссылка с текстом "ссылка на ответы"
        //2. кнопка с текстом спрятать ответы
        assertThat(_driver.findElement(By.xpath("//a[@data-target='answers_button' and text()[.='Ссылка на ответы']]"))).isNotNull();
        assertThat(_driver.findElement(By.xpath("//button[@id='hide_answ' and text()[.='Спрятать ответы']]"))).isNotNull();

    }

    @BeforeAll
    public static void ConfigSelenium(){
        WebDriverManager.edgedriver().setup();
        var options = new EdgeOptions();
        options.addArguments("headless","disable-gpu");
        _driver = new EdgeDriver();
    }

}
